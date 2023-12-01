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
    public void citydance_20231127a_regexPlusTime() {
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

        Assertions.assertEquals(1, calendarEvents.size());
        Assertions.assertEquals("Kerstgala", calendarEvents.get(0).subject());
        Assertions.assertEquals(LocalDateTime.of(2023, 12, 16, 20, 30, 0), calendarEvents.get(0).startDateTime());
        Assertions.assertEquals(LocalDateTime.of(2023, 12, 16, 23, 59, 0), calendarEvents.get(0).endDateTime());
    }

    @Test
    public void deDanssalon_20231127a_deBilt_regexDateOnly() {
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

        Assertions.assertEquals(13, calendarEvents.size());
        Assertions.assertEquals("", calendarEvents.get(0).subject());
        Assertions.assertEquals(LocalDateTime.of(2023, 10, 28, 14, 30, 0), calendarEvents.get(0).startDateTime());
        Assertions.assertEquals(LocalDateTime.of(2023, 10, 28, 18, 00, 0), calendarEvents.get(0).endDateTime());
    }

    @Test
    public void verhoeven_20231127a_2024_multipleDays() {
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

        Assertions.assertEquals(13, calendarEvents.size());
        Assertions.assertEquals("", calendarEvents.get(0).subject());
        Assertions.assertEquals(LocalDateTime.of(2024, 1, 6, 20, 30, 0), calendarEvents.get(0).startDateTime());
        Assertions.assertEquals(LocalDateTime.of(2024, 1, 6, 23, 59, 0), calendarEvents.get(0).endDateTime());
    }

    @Test
    public void wijgers_20231201a_shortDateNotation() {
        StringBuilder stringBuilder = new StringBuilder();
        List<CalendarEvent> calendarEvents = new CalendarSourceRegexScraper()
                .regex("([0-9][0-9]? (jan|feb|mrt|apr|mei|jun|jul|aug|sep|okt|nov|dec)) (Vrije Dansavond)")
                .subjectGroupIdx(3)
                .yearDefault(2024)
                .datePattern("d SMN")
                .shortMonthNotation("jan|feb|mrt|apr|mei|jun|jul|aug|sep|okt|nov|dec")
                .startDateGroupIdx(1)
                .endDateGroupIdx(1)
                .timePattern("HH:mm")
                .startTimeDefault("20:30")
                .endTimeDefault("23:59")
                .dateTimeLocale("NL")
                .removeChars("'")
                .scrapeUrl(this.getClass().getResource("/webSnapshots/wijgers_20231201a.html").toExternalForm())
                .scrapeBlockStart("Agenda")
                .generateEvents(stringBuilder);
        System.out.println(stringBuilder);
        System.out.println(calendarEvents);

        Assertions.assertEquals(6, calendarEvents.size());
        Assertions.assertEquals("Vrije Dansavond", calendarEvents.get(0).subject());
        Assertions.assertEquals(LocalDateTime.of(2024, 10, 28, 20, 30, 0), calendarEvents.get(0).startDateTime());
        Assertions.assertEquals(LocalDateTime.of(2024, 10, 28, 23, 59, 0), calendarEvents.get(0).endDateTime());
    }
}
