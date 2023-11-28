package nl.softworks.calendarAggregator.domain.entity;

import nl.softworks.calendarAggregator.domain.boundary.R;
import org.glassfish.jaxb.core.v2.TODO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class CalendarSourceRegexScraperTest {

    @Test
    public void citydance_20231127a() {
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
                .scrapeUrl(this.getClass().getResource("/webSnapshots/citydance_20231127a.html").toExternalForm())
                .generateEvents(stringBuilder);
        System.out.println(stringBuilder);
        System.out.println(calendarEvents);

        Assertions.assertEquals(1, calendarEvents.size());
        Assertions.assertEquals("Kerstgala", calendarEvents.get(0).subject());
        Assertions.assertEquals(LocalDateTime.of(2023, 12, 16, 20, 30, 0), calendarEvents.get(0).startDateTime());
        Assertions.assertEquals(LocalDateTime.of(2023, 12, 16, 23, 59, 0), calendarEvents.get(0).endDateTime());
    }

    @Test
    public void deDanssalon_20231127a_deBilt() {
        StringBuilder stringBuilder = new StringBuilder();
        List<CalendarEvent> calendarEvents = new CalendarSourceRegexScraper()
                .regex("([0-9][0-9]? +(januari|februari|maart|april|mei|juni|juli|augustus|september|oktober|november|december) +[0-9]{4})")
                .startDateGroupIdx(1)
                .endDateGroupIdx(1)
                .datePattern("d MMMM yyyy")
                .timePattern("HH:mm")
                .startTimeDefault("14:30")
                .endTimeDefault("18:00")
                .dateTimeLocale("NL")
                .scrapeUrl(this.getClass().getResource("/webSnapshots/deDanssalon_20231127a.html").toExternalForm())
                .scrapeBlockStart("Locatie de Bilt")
                .scrapeBlockEnd("Entree:")
                .generateEvents(stringBuilder);
        System.out.println(stringBuilder);
        System.out.println(calendarEvents);

        Assertions.assertEquals(13, calendarEvents.size());
        Assertions.assertEquals("", calendarEvents.get(0).subject());
        Assertions.assertEquals(LocalDateTime.of(2023, 10, 28, 14, 30, 0), calendarEvents.get(0).startDateTime());
        Assertions.assertEquals(LocalDateTime.of(2023, 10, 28, 18, 00, 0), calendarEvents.get(0).endDateTime());
    }

    @Test
    public void verhoeven_20231127a_2024() {
        StringBuilder stringBuilder = new StringBuilder();
        List<CalendarEvent> calendarEvents = new CalendarSourceMultipleDaysScraper()
                .regex("[0-9][0-9]? (januari|februari|maart|april|mei|juni|juli|augustus|september|oktober|november|december)")
                .yearDefault(2024)
                .datePattern("d MMMM")
                .timePattern("HH:mm")
                .startTimeDefault("20:30")
                .endTimeDefault("23:59")
                .dateTimeLocale("NL")
                .removeChars("*,")
                .scrapeUrl(this.getClass().getResource("/webSnapshots/verhoeven_20231127a.html").toExternalForm())
                .scrapeBlockStart("2024:")
                .scrapeBlockEnd("Zie agenda")
                .generateEvents(stringBuilder);
        System.out.println(stringBuilder);
        System.out.println(calendarEvents);

        Assertions.assertEquals(13, calendarEvents.size());
        Assertions.assertEquals("", calendarEvents.get(0).subject());
        Assertions.assertEquals(LocalDateTime.of(2024, 1, 6, 20, 30, 0), calendarEvents.get(0).startDateTime());
        Assertions.assertEquals(LocalDateTime.of(2024, 1, 6, 23, 59, 0), calendarEvents.get(0).endDateTime());
    }
}
