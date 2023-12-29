package nl.softworks.calendarAggregator.domain.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CalendarEventTest {

    @Test
    public void test() {
        // GIVEN: an event with a rrule
        CalendarEvent calendarEvent = new CalendarEvent()
                .subject("subject")
                .startDateTime(LocalDateTime.of(2023, 12, 01, 20, 0, 0))
                .endDateTime(LocalDateTime.of(2023, 12, 01, 23, 30, 0))
                .rrule("FREQ=MONTHLY;BYSETPOS=1;BYDAY=SU;INTERVAL=1;"); // first sunday of the month

        // WHEN: applying the rrule
        List<CalendarEvent> calendarEvents = calendarEvent.applyRRule();

        // THEN
        Assertions.assertEquals(5, calendarEvents.size());
        // THEN: assert first
        Assertions.assertEquals(LocalDateTime.of(2023, 12, 3, 20, 0, 0), calendarEvents.get(0).startDateTime());
        Assertions.assertEquals(LocalDateTime.of(2023, 12, 3, 23, 30, 0), calendarEvents.get(0).endDateTime());
        Assertions.assertEquals("subject", calendarEvents.get(0).subject());
        Assertions.assertEquals("", calendarEvents.get(0).rrule());
        // THEN: assert last
        Assertions.assertEquals(LocalDateTime.of(2024, 4, 7, 20, 0, 0), calendarEvents.get(4).startDateTime());
    }
}
