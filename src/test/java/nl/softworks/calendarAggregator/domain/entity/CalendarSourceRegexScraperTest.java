package nl.softworks.calendarAggregator.domain.entity;

import nl.softworks.calendarAggregator.domain.boundary.R;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class CalendarSourceRegexScraperTest {

//    @Test TODO: scrapeURL on local resource
    public void test() {
        StringBuilder stringBuilder = new StringBuilder();
        List<CalendarEvent> calendarEvents = new CalendarSourceRegexScraper()
                .regex("([a-zA-Z]*) +[a-z]{2}\\. ([0-9][0-9]? +(januari|februari|maart|april|mei|juni|juli|augustus|september|oktober|november|december) +[0-9]{4}) +van ([0-9]+:[0-9]+) tot ([0-9]+:[0-9]+)")
                .subjectGroupIdx(1)
                .startDateGroupIdx(2)
                .endDateGroupIdx(2)
                .datePattern("dd MMMM yyyy")
                .startTimeGroupIdx(4)
                .endTimeGroupIdx(5)
                .timePattern("HH:mm")
                .dateTimeLocale("NL")
                .generateEvents(stringBuilder);
        System.out.println(stringBuilder);
        System.out.println(calendarEvents);

        Assertions.assertEquals(3, calendarEvents.size());
        Assertions.assertEquals("DANSAVOND", calendarEvents.get(0).subject());
        Assertions.assertEquals(LocalDateTime.of(2023, 11, 18, 20, 00, 0), calendarEvents.get(0).startDateTime());
        Assertions.assertEquals(LocalDateTime.of(2023, 11, 18, 23, 59, 0), calendarEvents.get(0).endDateTime());
    }
}
