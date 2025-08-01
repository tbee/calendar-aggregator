package nl.softworks.calendarAggregator.domain.entity;

import nl.softworks.calendarAggregator.domain.boundary.SMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
                .scrapeBlockStart("Locatie de Bilt")
                .scrapeBlockEnd("Entree:")
                .scrapeUrl(this.getClass().getResource("/webSnapshots/deDanssalon_20231127a.html").toExternalForm())
                ;
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
                .scrapeBlockStart("2024:")
                .scrapeBlockEnd("Zie agenda")
                .scrapeUrl(this.getClass().getResource("/webSnapshots/verhoeven_20231127a.html").toExternalForm())
                ;
        calendarSource.localDateTimeNowSupplier = () -> LocalDateTime.of(2023, 12, 01, 12, 34, 56);
        new CalendarLocation().addCalendarSource(calendarSource);
        List<CalendarEvent> calendarEvents = calendarSource.generateEvents();

        Assertions.assertEquals(13, calendarEvents.size());
        Assertions.assertEquals("", calendarEvents.get(0).subject());
        assertLocalDateTimeNearestYear(LocalDateTime.of(2024, 1, 6, 20, 30, 0), calendarEvents.get(0).startDateTime());
        assertLocalDateTimeNearestYear(LocalDateTime.of(2024, 1, 6, 23, 59, 0), calendarEvents.get(0).endDateTime());
    }

    @Test
    public void stijl_20240531a_multipleDates() {
        CalendarSourceScraperBase calendarSource = new CalendarSourceRegexScraper() {}
                .regex("(Kerstgala|Vrijdansen[ a-zA-Z0-9]*)?(Colenbergh 1( + 2)?|Gymzaal Duurstedelaan A)? ([0-9][0-9]? +(januari|februari|maart|april|mei|juni|juli|augustus|september|oktober|november|december) [0-9]{4}) ([0-9]+:[0-9]+)")
                .subjectGroupIdx(1)
                .datePattern("d MMMM yyyy")
                .startDateGroupIdx(4)
                .endDateGroupIdx(4)
                .timePattern("HH:mm")
                .startTimeGroupIdx(6)
                .endTimeDefault("23:00")
                .dateTimeLocale("NL")
                .removeChars("'()-")
                .scrapeUrl(this.getClass().getResource("/webSnapshots/stijl_20240531a_multipleDates.html").toExternalForm());
        calendarSource.localDateTimeNowSupplier = () -> LocalDateTime.of(2024, 5, 31, 12, 34, 56);
        new CalendarLocation().addCalendarSource(calendarSource);
        List<CalendarEvent> calendarEvents = calendarSource.generateEvents();
        System.out.println(calendarSource.log());
        calendarEvents.forEach(e -> System.out.println(e));

        Assertions.assertEquals(14, calendarEvents.size());
        Assertions.assertEquals("Vrijdansen en Workshop West Coast Swing 31 mei 2024 HF Witte Colenbergh 1", calendarEvents.get(0).subject().trim());
        Assertions.assertEquals(LocalDateTime.of(2024, 5, 31, 19, 0, 0), calendarEvents.get(0).startDateTime());
        Assertions.assertEquals("Vrijdansen Maandagavond Zomer 2024 Gymzaal Duurstedelaan A", calendarEvents.get(13).subject().trim());
        Assertions.assertEquals(LocalDateTime.of(2024, 8, 26, 19, 30, 0), calendarEvents.get(13).startDateTime());
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
                .scrapeBlockStart("Agenda")
                .scrapeUrl(this.getClass().getResource("/webSnapshots/wijgers_20231201a.html").toExternalForm())
                ;
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
    public void danshotspot_20241021() {
        CalendarSourceScraperBase calendarSource = new CalendarSourceXmlScraper()
                .xpath("//event/name/text()[contains(., 'Vrijdansen') or contains(., 'Danscaf') or ends-with(., 'bal') or ends-with(., ' Gala')]")
                .startdateXpath("../../startsat")
                .enddateXpath("../../endsat")
                .starttimeXpath("../../startsat")
                .endtimeXpath("../../endsat")
                .subjectXpath(".")
                .format(CalendarSourceXmlScraper.Format.JSON)
                .datePattern("yyyy-MM-dd'T'HH:mm:ss.nX")
                .timePattern("yyyy-MM-dd'T'HH:mm:ss.nX")
                .dateTimeLocale("NL")
                .scrapeUrl(this.getClass().getResource("/webSnapshots/danshotspot_20241021a.json").toExternalForm());
        calendarSource.localDateTimeNowSupplier = () -> LocalDateTime.of(2024, 10, 21, 12, 34, 56);
        new CalendarLocation().addCalendarSource(calendarSource);
        List<CalendarEvent> calendarEvents = calendarSource.generateEvents();
        System.out.println(calendarSource.log());
        System.out.println(calendarEvents);

        Assertions.assertEquals(7, calendarEvents.size());
        Assertions.assertEquals("Danscafe Ginger", calendarEvents.get(0).subject());
        assertLocalDateTimeNearestYear(LocalDateTime.of(2024, 10, 22, 12, 0, 0), calendarEvents.get(0).startDateTime());
        assertLocalDateTimeNearestYear(LocalDateTime.of(2024, 10, 22, 15, 0, 0), calendarEvents.get(0).endDateTime());
    }

    @Test
    public void dancefever_20250628() {
        SMock.populate();

        CalendarSourceICal calendarSource = new CalendarSourceICal()
                .regex(".*(avond|gala).*")
                .icalUrl(this.getClass().getResource("/webSnapshots/dancefever_20250628.ical").toExternalForm());
        calendarSource.localDateTimeNowSupplier = () -> LocalDateTime.of(2025, 06, 21, 12, 34, 56);
        new CalendarLocation()
                .timezone(new Timezone().name(ZoneId.of("Europe/Amsterdam").getId()))
                .addCalendarSource(calendarSource);
        List<CalendarEvent> calendarEvents = calendarSource.generateEvents();
        System.out.println(calendarSource.log());
        System.out.println(calendarEvents);

        Assertions.assertEquals(1, calendarEvents.size());
        Assertions.assertEquals("Vrijdansavond Dance Fever", calendarEvents.get(0).subject());
        assertLocalDateTimeNearestYear(LocalDateTime.of(2025, 6, 27, 20, 0, 0), calendarEvents.get(0).startDateTime());
        assertLocalDateTimeNearestYear(LocalDateTime.of(2025, 6, 27, 23, 30, 0), calendarEvents.get(0).endDateTime());
    }

    @Test
    public void vidazevenaar_20250629a() {
        SMock.populate();

        CalendarSourceICal calendarSource = new CalendarSourceICal()
                .regex(".*(oudjaarsavond|Nieuwjaarsbal|Dansavond|Danscaf|Dansmiddag|Openingsavond|Workshop|Techniek).*")
                .icalUrl(this.getClass().getResource("/webSnapshots/vidazevenaar_20250629a.ical").toExternalForm());
        calendarSource.localDateTimeNowSupplier = () -> LocalDateTime.of(2025, 06, 21, 12, 34, 56);
        new CalendarLocation()
                .timezone(new Timezone().name(ZoneId.of("Europe/Amsterdam").getId()))
                .addCalendarSource(calendarSource);
        List<CalendarEvent> calendarEvents = calendarSource.generateEvents();
        System.out.println(calendarSource.log());
        System.out.println(calendarEvents);

        Assertions.assertEquals(11, calendarEvents.size());
        Assertions.assertEquals("Dansavond Zaterdag 9 augustus", calendarEvents.get(0).subject());
        assertLocalDateTimeNearestYear(LocalDateTime.of(2025, 8, 9, 21, 0, 0), calendarEvents.get(0).startDateTime());
        assertLocalDateTimeNearestYear(LocalDateTime.of(2025, 8, 9, 23, 30, 0), calendarEvents.get(0).endDateTime());
    }

    private void assertLocalDateTimeNearestYear(LocalDateTime expectedLocalDateTime, LocalDateTime actualLocalDateTime) {
        int actualYear = actualLocalDateTime.getYear();

        LocalDateTime expectedLocalDateTimeIgnoreYear = expectedLocalDateTime.withYear(actualYear);
        Assertions.assertEquals(expectedLocalDateTimeIgnoreYear, actualLocalDateTime);

        int nowYear = LocalDate.now().getYear();
        Assertions.assertTrue(actualYear == nowYear - 1 || actualYear == nowYear || actualYear == nowYear + 1);
    }
}
