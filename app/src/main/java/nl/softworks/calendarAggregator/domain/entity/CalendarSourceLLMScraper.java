package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import nl.softworks.calendarAggregator.domain.interfaces.S;
import nl.softworks.calendarAggregator.domain.service.CalendarEventsXmlParser;
import nl.softworks.calendarAggregator.domain.service.OpenRouterService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;

@Entity
@DiscriminatorValue("ai")
public class CalendarSourceLLMScraper extends CalendarSourceScraperBaseHTML {

    public String type() {
        return "LLM";
    }

    @NotNull
    @Column(name = "subject")
    private String model = OpenRouterService.AUTO_MODEL;
    static public final String MODEL = "model";
    public String model() {
        return model;
    }
    public CalendarSourceLLMScraper model(String v) {
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
    public CalendarSourceLLMScraper prompt(String v) {
        this.prompt = v;
        return this;
    }

    @Override
    public Set<CalendarEvent> generateEvents() {
        try {
            super.generateEvents();
            if (!isEnabled()) {
                return calendarEvents;
            }

            // Get contents
            String content = readScrapeUrlAsHtml();
            if (content.isBlank()) {
                status("No contents");
                return Set.of();
            }
            content = sanatizeContent(content);

            // Apply prompt
            String systemPrompt = """
                    You are a web scraper, your task is to extract dancing events from websites.
                    
                    Today is %s. Resolve dates without a year against today, and never return events in the past.
                    If events do not contain a time, make a reasonable assumption. For example: events in the evening usually start at 20:00 and end around 23:00.
                    Ignore events that are about getting people to start taking lessons, like "kennismakingsles" of "proefles", or the start of a new season or block of lessons.
                    Ignore events that actually are indicators there are no lessons ("Geen les / Geen lessen").
                    
                    Return the events in exactly this XML structure:
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
                    %prompt%

                    Below is the HTML content of the webpage at %url%.
                    Extract the events from it and ignore any HTML that is not related to events.
                    %content%
                    """
                    .replace("%prompt%", prompt)
                    .replace("%url%", scrapeUrl)
                    .replace("%content%", content);
            logAppend(systemPrompt + "\n" + userPrompt + "\n");

            String selectedModel = (model == null || model.isBlank()) ? OpenRouterService.AUTO_MODEL : model;
            logAppend("Model: " + selectedModel + "\n");

            ChatResponse chatResponse = ChatClient
                    .create(S.chatModel())
                    .prompt()
                    .options(OpenAiChatOptions.builder().model(selectedModel))
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .chatResponse();
            String response = chatResponse == null ? null : chatResponse.getResult().getOutput().getText();
            logAppend("Resolved model: " + (chatResponse == null ? "unknown" : chatResponse.getMetadata().getModel()) + "\n");
            logAppend(response + "\n");

            for (CalendarEvent calendarEvent : CalendarEventsXmlParser.parse(response, this::logAppend)) {
                addCalendarEvent(calendarEvent);
                logAppend("Added event: " + calendarEvent + "\n");
            }

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
}
