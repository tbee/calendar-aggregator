package nl.softworks.calendarAggregator.domain.service;

import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalendarEventsXmlParserTest {

    private final List<String> log = new ArrayList<>();

    @Test
    void parsesEvent() {
        List<CalendarEvent> events = CalendarEventsXmlParser.parse("""
                <events>
                    <event>
                        <startDate>2026-03-14</startDate>
                        <endDate>2026-03-15</endDate>
                        <startTime>20:00</startTime>
                        <endTime>01:00</endTime>
                        <description>Spring Ball</description>
                    </event>
                </events>
                """, log::add);

        assertEquals(1, events.size());
        CalendarEvent event = events.get(0);
        assertEquals("Spring Ball", event.subject());
        assertEquals(LocalDate.of(2026, 3, 14), event.startDateTime().toLocalDate());
        assertEquals(LocalTime.of(20, 0), event.startDateTime().toLocalTime());
        assertEquals(LocalDate.of(2026, 3, 15), event.endDateTime().toLocalDate());
        assertEquals(LocalTime.of(1, 0), event.endDateTime().toLocalTime());
    }

    @Test
    void parsesEventWithoutEndAsOneHour() {
        List<CalendarEvent> events = CalendarEventsXmlParser.parse("""
                <events>
                    <event>
                        <startDate>2026-03-14</startDate>
                        <startTime>20:00</startTime>
                        <description>Workshop</description>
                    </event>
                </events>
                """, log::add);

        assertEquals(1, events.size());
        CalendarEvent event = events.get(0);
        assertEquals(LocalTime.of(20, 0), event.startDateTime().toLocalTime());
        assertEquals(LocalTime.of(21, 0), event.endDateTime().toLocalTime());
    }

    @Test
    void stripsMarkdownCodeFences() {
        List<CalendarEvent> events = CalendarEventsXmlParser.parse("""
                ```xml
                <events>
                    <event>
                        <startDate>2026-03-14</startDate>
                        <startTime>20:00</startTime>
                        <description>Fenced</description>
                    </event>
                </events>
                ```
                """, log::add);

        assertEquals(1, events.size());
        assertEquals("Fenced", events.get(0).subject());
    }

    @Test
    void skipsEventWithoutStartDate() {
        List<CalendarEvent> events = CalendarEventsXmlParser.parse("""
                <events>
                    <event>
                        <description>No date</description>
                    </event>
                    <event>
                        <startDate>2026-03-14</startDate>
                        <description>With date</description>
                    </event>
                </events>
                """, log::add);

        assertEquals(1, events.size());
        assertEquals("With date", events.get(0).subject());
        assertTrue(log.stream().anyMatch(line -> line.contains("without a start date")));
    }

    @Test
    void returnsEmptyForBlankResponse() {
        assertTrue(CalendarEventsXmlParser.parse("", log::add).isEmpty());
        assertTrue(CalendarEventsXmlParser.parse(null, log::add).isEmpty());
    }
}