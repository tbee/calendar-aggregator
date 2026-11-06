package nl.softworks.calendarAggregator.domain.service;

import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Parses the XML event structure that the LLM based scrapers ask the model to return.
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
public final class CalendarEventsXmlParser {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private CalendarEventsXmlParser() {
    }

    public static List<CalendarEvent> parse(String response, Consumer<String> log) {
        List<CalendarEvent> calendarEvents = new ArrayList<>();
        if (response == null || response.isBlank()) {
            return calendarEvents;
        }

        Document doc = Jsoup.parse(stripCodeFences(response), "", Parser.xmlParser());
        for (Element eventElement : doc.getElementsByTag("event")) {
            String startDateString = tagText(eventElement, "startDate");
            String endDateString = tagText(eventElement, "endDate");
            String startTimeString = tagText(eventElement, "startTime");
            String endTimeString = tagText(eventElement, "endTime");
            String description = tagText(eventElement, "description");

            if (startDateString.isBlank()) {
                log.accept("Skipping event without a start date: " + eventElement + "\n");
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
            if (endDateTime.isBefore(startDateTime)) {
                // an end time on or after midnight
                endDateTime = endDateTime.plusDays(1);
                log.accept("End moment < start moment, added one day: " + endDateTime + "\n");
            }
            if (endDateTime.isBefore(startDateTime)) {
                throw new RuntimeException("End date should be after start: " + startDateTime + " < " + endDateTime);
            }

            calendarEvents.add(new CalendarEvent()
                    .subject(description)
                    .startDateTime(startDateTime)
                    .endDateTime(endDateTime));
        }
        return calendarEvents;
    }

    private static String tagText(Element parent, String tagName) {
        Element element = parent.getElementsByTag(tagName).first();
        return element == null ? "" : element.text().trim();
    }

    /**
     * LLMs frequently wrap their output in markdown code fences (```xml ... ```);
     * strip those so the XML can be parsed.
     */
    private static String stripCodeFences(String response) {
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