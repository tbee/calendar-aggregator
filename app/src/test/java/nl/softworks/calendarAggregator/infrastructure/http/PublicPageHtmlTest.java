package nl.softworks.calendarAggregator.infrastructure.http;

import nl.softworks.calendarAggregator.domain.entity.Settings;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The two public pages as HTML: one head, one body, and a head a search engine can read.
 * <p>
 * This is the regression test for the layout bug the pages had: month.html and list.html passed their own
 * {@code <head>} and {@code <body>} into the layout, which inserted them inside its own, so the served document
 * had two of each. The engine renders the same templates the application resolves, straight from the classpath.
 */
class PublicPageHtmlTest {

    private static final String BASE_URL = "https://where.shallwedance.org";

    private static SpringTemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCacheable(false);
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    private static Settings settings() {
        return new Settings()
                .title("Where Shall We Dance?")
                .subtitle("Ballroom and latin")
                .disclaimer("Always check if events actually take place on the respective websites.");
    }

    /** The model the controller builds, with no events and no labels: enough to render the whole document. */
    private static Context context(PageMeta.View view, boolean filtered, String filterQuerySuffix) {
        LocalDate start = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
        List<LocalDate> dates = start.datesUntil(start.plusWeeks(6)).toList();
        List<List<LocalDate>> weeks = new java.util.ArrayList<>();
        for (int i = 0; i < dates.size(); i += 7) {
            weeks.add(dates.subList(i, Math.min(i + 7, dates.size())));
        }
        Map<LocalDate, List<Object>> dateToEvents = dates.stream().collect(Collectors.toMap(date -> date, date -> List.of()));

        Context context = new Context();
        context.setVariable("settings", settings());
        context.setVariable("pageMeta", PageMeta.of(settings(), BASE_URL, view, filtered));
        context.setVariable("baseUrl", BASE_URL);
        context.setVariable("filterQuerySuffix", filterQuerySuffix);
        context.setVariable("request", new MockHttpServletRequest());
        context.setVariable("labels", List.of());
        context.setVariable("labelsInclude", List.of());
        context.setVariable("labelsExclude", List.of());
        context.setVariable("labelGroups", List.of());
        context.setVariable("dateToEvents", dateToEvents);
        context.setVariable("weekOfDates", weeks);
        context.setVariable("eventToText", new HashMap<>());
        context.setVariable("eventToTooltip", new HashMap<>());
        context.setVariable("eventToWhen", new HashMap<>());
        context.setVariable("eventToWhat", new HashMap<>());
        context.setVariable("eventToLabels", new HashMap<>());
        context.setVariable("mmmdd", DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH));
        context.setVariable("hhmm", DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH));
        context.setVariable("today", LocalDate.now());
        context.setVariable("startOfMonth", LocalDate.now().withDayOfMonth(1));
        context.setVariable("numberOfWeeks", 6);
        return context;
    }

    private static String render(String template, Context context) {
        return templateEngine().process(template, context);
    }

    private static int count(String html, String needle) {
        return html.split(java.util.regex.Pattern.quote(needle), -1).length - 1;
    }

    @Test
    void theCalendarIsOneValidDocument() {
        String html = render("month", context(PageMeta.View.CALENDAR, false, ""));

        // the layout bug: exactly one head and one body, wherever the page's own fragments end up
        assertEquals(1, count(html, "<head>"), "one <head>");
        assertEquals(1, count(html, "</head>"), "one </head>");
        assertEquals(1, count(html, "<body"), "one <body>");
        assertEquals(1, count(html, "</body>"), "one </body>");
    }

    @Test
    void theCalendarCarriesTheHeadASearchEngineNeeds() {
        String html = render("month", context(PageMeta.View.CALENDAR, false, ""));

        assertTrue(html.contains("<meta charset=\"utf-8\">"));
        assertTrue(html.contains("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"));
        assertTrue(html.contains("<title>Where Shall We Dance? – Ballroom and latin dance calendar</title>"));
        assertTrue(html.contains("<meta name=\"description\" content=\"Ballroom and latin. Always check"));
        assertTrue(html.contains("<link rel=\"canonical\" href=\"" + BASE_URL + "/\">"));
        assertTrue(html.contains("<meta property=\"og:url\" content=\"" + BASE_URL + "/\">"));
        assertTrue(html.contains("<link rel=\"alternate\" type=\"text/calendar\" href=\"" + BASE_URL + "/ical\">"));
        assertTrue(html.contains("\"@type\":\"WebSite\""));
        // the bare calendar may be indexed
        assertFalse(html.contains("name=\"robots\""));
    }

    @Test
    void aFilteredCalendarIsNoIndexedAndKeepsTheFilterInItsLinks() {
        String html = render("month", context(PageMeta.View.CALENDAR, true, "?lat=52.3676&lon=4.9041"));

        assertTrue(html.contains("<meta name=\"robots\" content=\"noindex, follow\">"));
        // the canonical still points at the calendar itself, so the variant does not compete
        assertTrue(html.contains("<link rel=\"canonical\" href=\"" + BASE_URL + "/\">"));
        assertTrue(html.contains("href=\"" + BASE_URL + "/list?lat=52.3676&amp;lon=4.9041\""));
        // the calendar keeps its own filters too, and without doubling the question mark
        assertTrue(html.contains("href=\"" + BASE_URL + "/?lat=52.3676&amp;lon=4.9041\""));
        assertFalse(html.contains("/??"));
    }

    @Test
    void withoutFiltersEveryInternalLinkIsClean() {
        String html = render("month", context(PageMeta.View.CALENDAR, false, ""));

        assertTrue(html.contains("href=\"" + BASE_URL + "/list\""), "the list is linked without a query string");
        assertTrue(html.contains("href=\"" + BASE_URL + "/\""), "the calendar is linked without a query string");
        assertFalse(html.contains("/list?"));
        assertFalse(html.contains("/ical?"));
    }

    @Test
    void theListIsOneDocumentAndIsNoIndexed() {
        String html = render("list", context(PageMeta.View.LIST, false, ""));

        assertEquals(1, count(html, "<head>"));
        assertEquals(1, count(html, "<body"));
        assertTrue(html.contains("<title>Where Shall We Dance? – Ballroom and latin dance events</title>"));
        assertTrue(html.contains("<meta name=\"robots\" content=\"noindex, follow\">"));
    }
}
