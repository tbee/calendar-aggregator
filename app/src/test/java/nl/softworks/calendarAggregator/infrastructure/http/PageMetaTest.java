package nl.softworks.calendarAggregator.infrastructure.http;

import nl.softworks.calendarAggregator.domain.entity.Settings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What the head of a public page tells a search engine. Pure logic, no database: the calendar is the page,
 * every variant of it and the list view are not.
 */
class PageMetaTest {

    private static final String BASE_URL = "https://where.shallwedance.org";

    private static Settings settings(String disclaimer) {
        return new Settings()
                .title("Where Shall We Dance?")
                .subtitle("Ballroom and latin")
                .disclaimer(disclaimer);
    }

    @Test
    void theCalendarIsThePageThatMayBeIndexed() {
        PageMeta meta = PageMeta.of(settings(""), BASE_URL, PageMeta.View.CALENDAR, false);

        assertEquals("Where Shall We Dance? – Ballroom and latin dance calendar", meta.title());
        assertNull(meta.robots(), "a search engine may index the bare calendar");
        assertEquals(BASE_URL + "/", meta.canonicalUrl());
    }

    @Test
    void aFilteredCalendarIsAVariantThatMayNotBeIndexed() {
        PageMeta meta = PageMeta.of(settings(""), BASE_URL, PageMeta.View.CALENDAR, true);

        assertEquals("noindex, follow", meta.robots());
        // it still points at the calendar itself, so the variants do not compete
        assertEquals(BASE_URL + "/", meta.canonicalUrl());
    }

    @Test
    void theListViewIsTheSameAgendaInAnotherFormAndMayNotBeIndexed() {
        assertEquals("noindex, follow", PageMeta.of(settings(""), BASE_URL, PageMeta.View.LIST, false).robots());
    }

    @Test
    void theDescriptionIsTheSubtitlePlusTheDisclaimerCutToWhatASearchResultShows() {
        String disclaimer = "Always check if events actually take place, and at what time exactly, on the "
                + "respective websites. These dates are intended for easy planning, but may be outdated.";
        PageMeta meta = PageMeta.of(settings(disclaimer), BASE_URL, PageMeta.View.CALENDAR, false);

        assertTrue(meta.description().startsWith("Ballroom and latin. Always check"));
        assertTrue(meta.description().length() <= 156, "was: " + meta.description().length());
        assertTrue(meta.description().endsWith("…"));
    }

    @Test
    void aShortDescriptionIsKeptWhole() {
        PageMeta meta = PageMeta.of(settings("Free dancing and courses."), BASE_URL, PageMeta.View.CALENDAR, false);

        assertEquals("Ballroom and latin. Free dancing and courses.", meta.description());
    }

    @Test
    void theStructuredDataNamesTheSiteAndItsAddress() {
        String structuredData = PageMeta.of(settings(""), BASE_URL, PageMeta.View.CALENDAR, false).structuredData();

        assertTrue(structuredData.contains("\"@type\":\"WebSite\""));
        assertTrue(structuredData.contains("\"name\":\"Where Shall We Dance?\""));
        assertTrue(structuredData.contains("\"url\":\"" + BASE_URL + "/\""));
    }

    @Test
    void aSiteWithoutALogoHasNoOpenGraphImage() {
        assertNull(PageMeta.of(settings(""), BASE_URL, PageMeta.View.CALENDAR, false).ogImageUrl());

        Settings withLogo = settings("").logo(new byte[]{1, 2, 3});
        assertEquals(BASE_URL + "/logo", PageMeta.of(withLogo, BASE_URL, PageMeta.View.CALENDAR, false).ogImageUrl());
    }

    @Test
    void aSiteWithoutSettingsStillGetsAUsableHead() {
        PageMeta meta = PageMeta.of(new Settings(), BASE_URL, PageMeta.View.CALENDAR, false);

        assertTrue(meta.title().contains("Where Shall We Dance?"));
        assertFalse(meta.description().isBlank());
        assertEquals(BASE_URL + "/", meta.canonicalUrl());
        assertNotNull(meta.structuredData());
    }
}
