package nl.softworks.calendarAggregator.domain.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CalendarSourcesTest {

    @Test
    public void citydance_20231127a_regexPlusTime() {
        CalendarSourceScraperBase calendarSource = new CalendarSourceRegexScraper()
                .regex("([a-zA-Z]*) +[a-z]{2}\\. ([0-9][0-9]? +(januari|februari|maart|april|mei|juni|juli|augustus|september|oktober|november|december) +[0-9]{4}) +van ([0-9]+:[0-9]+) tot ([0-9]+:[0-9]+)")
                .subjectGroupIdx(1)
                .startDateGroupIdx(2)
                .endDateGroupIdx(2)
                .datePattern("dd MMMM yyyy")
                .startTimeGroupIdx(4)
                .endTimeGroupIdx(5)
                .timePattern("HH:mm")
                .dateTimeLocale("NL")
                .scrapeUrl(this.getClass().getResource("/webSnapshots/citydance_20231127a.html").toExternalForm());
        calendarSource.localDateTimeNowSupplier = () -> LocalDateTime.of(2023, 12, 01, 12, 34, 56);
        new CalendarLocation().addCalendarSource(calendarSource);
        List<CalendarEvent> calendarEvents = calendarSource.generateEvents();
        Assertions.assertEquals(1, calendarEvents.size());
        Assertions.assertEquals("Kerstgala", calendarEvents.get(0).subject());
        Assertions.assertEquals(LocalDateTime.of(2023, 12, 16, 20, 30, 0), calendarEvents.get(0).startDateTime());
        Assertions.assertEquals(LocalDateTime.of(2023, 12, 16, 23, 59, 0), calendarEvents.get(0).endDateTime());
    }

    @Test
    public void deDanssalon_20231127a_deBilt_regexDateOnly() {
        CalendarSourceScraperBase calendarSource = new CalendarSourceRegexScraper()
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
                .scrapeBlockEnd("Entree:");
        calendarSource.localDateTimeNowSupplier = () -> LocalDateTime.of(2023, 10, 01, 12, 34, 56);
        new CalendarLocation().addCalendarSource(calendarSource);
        List<CalendarEvent> calendarEvents = calendarSource.generateEvents();

        Assertions.assertEquals(13, calendarEvents.size());
        Assertions.assertEquals("", calendarEvents.get(0).subject());
        Assertions.assertEquals(LocalDateTime.of(2023, 10, 28, 14, 30, 0), calendarEvents.get(0).startDateTime());
        Assertions.assertEquals(LocalDateTime.of(2023, 10, 28, 18, 00, 0), calendarEvents.get(0).endDateTime());
    }

    @Test
    public void verhoeven_20231127a_2024_multipleDays() {
        CalendarSourceScraperBase calendarSource = new CalendarSourceMultipleDaysScraper() {}
                .regex("[0-9][0-9]? (januari|februari|maart|april|mei|juni|juli|augustus|september|oktober|november|december)")
                .nearestYear(true)
                .datePattern("d MMMM")
                .timePattern("HH:mm")
                .startTimeDefault("20:30")
                .endTimeDefault("23:59")
                .dateTimeLocale("NL")
                .removeChars("*,")
                .scrapeUrl(this.getClass().getResource("/webSnapshots/verhoeven_20231127a.html").toExternalForm())
                .scrapeBlockStart("2024:")
                .scrapeBlockEnd("Zie agenda");
        calendarSource.localDateTimeNowSupplier = () -> LocalDateTime.of(2023, 12, 01, 12, 34, 56);
        new CalendarLocation().addCalendarSource(calendarSource);
        List<CalendarEvent> calendarEvents = calendarSource.generateEvents();

        Assertions.assertEquals(13, calendarEvents.size());
        Assertions.assertEquals("", calendarEvents.get(0).subject());
        assertLocalDateTimeNearestYear(LocalDateTime.of(2024, 1, 6, 20, 30, 0), calendarEvents.get(0).startDateTime());
        assertLocalDateTimeNearestYear(LocalDateTime.of(2024, 1, 6, 23, 59, 0), calendarEvents.get(0).endDateTime());
    }

    @Test
    public void wijgers_20231201a_shortDateNotation() {
        CalendarSourceScraperBase calendarSource = new CalendarSourceRegexScraper()
                .regex("([0-9][0-9]? (jan|feb|mrt|apr|mei|jun|jul|aug|sep|okt|nov|dec)) (Vrije Dansavond)")
                .subjectGroupIdx(3)
                .nearestYear(true)
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
                .scrapeBlockStart("Agenda");
        calendarSource.localDateTimeNowSupplier = () -> LocalDateTime.of(2023, 10, 01, 12, 34, 56);
        new CalendarLocation().addCalendarSource(calendarSource);
        List<CalendarEvent> calendarEvents = calendarSource.generateEvents();
        System.out.println(calendarSource.log());
        System.out.println(calendarEvents);

        Assertions.assertEquals(6, calendarEvents.size());
        Assertions.assertEquals("Vrije Dansavond", calendarEvents.get(0).subject());
        assertLocalDateTimeNearestYear(LocalDateTime.of(2024, 10, 28, 20, 30, 0), calendarEvents.get(0).startDateTime());
        assertLocalDateTimeNearestYear(LocalDateTime.of(2024, 10, 28, 23, 59, 0), calendarEvents.get(0).endDateTime());
    }

    @Test
    public void danshotspot_20240203() {
        CalendarSourceScraperBase calendarSource = new CalendarSourceXmlScraper()
                .xpath("//event/name/text/text()[contains(., 'Vrijdansen') or ends-with(., 'bal')]")
                .startdateXpath("../../../start/local")
                .enddateXpath("../../../end/local")
                .starttimeXpath("../../../start/local")
                .endtimeXpath("../../../end/local")
                .subjectXpath(".")
                .jsonToXml(true)
                .datePattern("yyyy-MM-dd'T'HH:mm:ss")
                .timePattern("yyyy-MM-dd'T'HH:mm:ss")
                .dateTimeLocale("NL")
                .scrapeUrl(this.getClass().getResource("/webSnapshots/danshotspot_20240203.html").toExternalForm());
        calendarSource.localDateTimeNowSupplier = () -> LocalDateTime.of(2023, 12, 01, 12, 34, 56);
        new CalendarLocation().addCalendarSource(calendarSource);
        List<CalendarEvent> calendarEvents = calendarSource.generateEvents();
        System.out.println(calendarSource.log());
        System.out.println(calendarEvents);

        Assertions.assertEquals(7, calendarEvents.size());
        Assertions.assertEquals("Vrijdansen, stijldansavond", calendarEvents.get(0).subject());
        assertLocalDateTimeNearestYear(LocalDateTime.of(2024, 5, 16, 19, 0, 0), calendarEvents.get(0).startDateTime());
        assertLocalDateTimeNearestYear(LocalDateTime.of(2024, 5, 16, 22, 0, 0), calendarEvents.get(0).endDateTime());
    }

    private void assertLocalDateTimeNearestYear(LocalDateTime expectedLocalDateTime, LocalDateTime actualLocalDateTime) {
        int actualYear = actualLocalDateTime.getYear();

        LocalDateTime expectedLocalDateTimeIgnoreYear = expectedLocalDateTime.withYear(actualYear);
        Assertions.assertEquals(expectedLocalDateTimeIgnoreYear, actualLocalDateTime);

        int nowYear = LocalDate.now().getYear();
        Assertions.assertTrue(actualYear == nowYear - 1 || actualYear == nowYear || actualYear == nowYear + 1);
    }
}
