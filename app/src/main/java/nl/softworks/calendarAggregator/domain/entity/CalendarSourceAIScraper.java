package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import nl.softworks.calendarAggregator.domain.boundary.S;
import nl.softworks.calendarAggregator.domain.service.OpenRouterService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Entity
@DiscriminatorValue("ai")
public class CalendarSourceAIScraper extends CalendarSourceScraperBaseHTML {

    public String type() {
        return "AI";
    }

    @NotNull
    @Column(name = "subject")
    private String model = OpenRouterService.AUTO_MODEL;
    static public final String MODEL = "model";
    public String model() {
        return model;
    }
    public CalendarSourceAIScraper model(String v) {
        this.model = v;
        return this;
    }

    @NotNull
    @Column(name = "regex")
    private String prompt;
    static public final String PROMPT = "prompt";
    public String prompt() {
        return prompt;
    }
    public CalendarSourceAIScraper prompt(String v) {
        this.prompt = v;
        return this;
    }

    /**
     * In addition to the base sanitizing, strip out script and style tags so their
     * (often large and irrelevant) content is not sent to the LLM.
     */
    @Override
    protected String sanatizeContent(String content) {
        Document doc = Jsoup.parse(content);
        doc.select("script, style").remove();
        return super.sanatizeContent(doc.text());
    }

    @Override
    public Set<CalendarEvent> generateEvents() {
        try {
            super.generateEvents();
            if (!isEnabled()) {
                return calendarEvents;
            }

            // Get contents
            String content = readScrapeUrlHTML();
            if (content.isBlank()) {
                status("No contents");
                return Set.of();
            }
            content = sanatizeContent(content);

            // Apply prompt
            String systemPrompt = """
                    You are a web scraper, your task is to extract dancing events from websites.
                    You need to find start and end moments for the events and return them as a XML structure similar to this:
                    <events>
                        <event>
                            <startDate>2024-01-01</startDate>
                            <endDate>2024-01-02</endDate>
                            <startTime>10:00</startTime>
                            <endTime>12:00</endTime>
                            <description>Dancing is fun</description>
                        </event>
                    </events>
                    Dates must be in the format yyyy-MM-dd.
                    Times must be in the format HH:mm.
                    Only return the XML structure, nothing else.
                    """;
            String userPrompt = """
                    %promp%

                    Below is the HTML content of the webpage at %url%, enclosed in <html> tags.
                    Extract the events from it and ignore any HTML that is not related to events.
                    <html>
                    %content%
                    </html>
                    """
                    .replace("%promp%", prompt)
                    .replace("%url%", url())
                    .replace("%content%", content);
            logAppend(systemPrompt + "\n" + userPrompt + "\n");

            String selectedModel = (model == null || model.isBlank()) ? OpenRouterService.AUTO_MODEL : model;
            logAppend("Model: " + selectedModel + "\n");

            String response = ChatClient
                    .create(S.chatModel())
                    .prompt()
                    .options(OpenAiChatOptions.builder().model(selectedModel))
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();
            logAppend(response + "\n");

            parseEvents(response);

            dropExpiredEvents();
            sanatizeEvents();
            logAppend("Done\n");

            // set status
            if (calendarEvents().isEmpty()) {
                status("No events");
                return Set.of();
            }

            return calendarEvents();
        }
        catch (RuntimeException e) {
            status(e.getMessage());
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            logAppend(stringWriter.toString());
            throw e;
        }
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Parse the XML returned by the LLM into CalendarEvents.
     * Expected structure:
     * <pre>
     * &lt;events&gt;
     *     &lt;event&gt;
     *         &lt;startDate&gt;2024-01-01&lt;/startDate&gt;
     *         &lt;endDate&gt;2024-01-02&lt;/endDate&gt;
     *         &lt;startTime&gt;10:00&lt;/startTime&gt;
     *         &lt;endTime&gt;12:00&lt;/endTime&gt;
     *         &lt;description&gt;Dancing is fun&lt;/description&gt;
     *     &lt;/event&gt;
     * &lt;/events&gt;
     * </pre>
     */
    private void parseEvents(String response) {
        Document doc = Jsoup.parse(stripCodeFences(response), "", Parser.xmlParser());
        for (Element eventElement : doc.getElementsByTag("event")) {
            String startDateString = tagText(eventElement, "startDate");
            String endDateString = tagText(eventElement, "endDate");
            String startTimeString = tagText(eventElement, "startTime");
            String endTimeString = tagText(eventElement, "endTime");
            String description = tagText(eventElement, "description");

            if (startDateString.isBlank()) {
                logAppend("Skipping event without a start date: " + eventElement + "\n");
                continue;
            }

            LocalDate startDate = LocalDate.parse(startDateString, DATE_FORMATTER);
            LocalDate endDate = endDateString.isBlank() ? startDate : LocalDate.parse(endDateString, DATE_FORMATTER);
            LocalTime startTime = startTimeString.isBlank() ? LocalTime.MIDNIGHT : LocalTime.parse(startTimeString, TIME_FORMATTER);
            LocalTime endTime = endTimeString.isBlank() ? startTime : LocalTime.parse(endTimeString, TIME_FORMATTER);

            LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
            LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);
            if (!endDateTime.isAfter(startDateTime)) {
                // no (valid) end supplied: default to one hour after start
                endDateTime = startDateTime.plusHours(1);
            }
            endDateTime = makeSureEndIsAfterStart(startDateTime, endDateTime);

            CalendarEvent calendarEvent = new CalendarEvent()
                    .subject(description)
                    .startDateTime(startDateTime)
                    .endDateTime(endDateTime);
            addCalendarEvent(calendarEvent);
            logAppend("Added event: " + calendarEvent + "\n");
        }
    }

    private String tagText(Element parent, String tagName) {
        Element element = parent.getElementsByTag(tagName).first();
        return element == null ? "" : element.text().trim();
    }

    /**
     * LLMs frequently wrap their output in markdown code fences (```xml ... ```);
     * strip those so the XML can be parsed.
     */
    private String stripCodeFences(String response) {
        String xml = response.trim();
        if (xml.startsWith("```")) {
            int firstNewline = xml.indexOf('\n');
            xml = firstNewline < 0 ? "" : xml.substring(firstNewline + 1);
            if (xml.endsWith("```")) {
                xml = xml.substring(0, xml.length() - 3);
            }
        }
        return xml.trim();
    }
}
