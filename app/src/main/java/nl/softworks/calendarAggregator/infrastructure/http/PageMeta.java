package nl.softworks.calendarAggregator.infrastructure.http;

import nl.softworks.calendarAggregator.domain.entity.Settings;

/**
 * What the head of a public page tells a search engine: the title, the description, the address a crawler
 * should store, and whether the page may be indexed at all.
 * <p>
 * Only the calendar is a page of its own. The list shows the same events in another form, and every filter
 * (lat/lon/distance/labels, and any parameter added later) makes a variant that should not compete in the
 * index: those answer {@code noindex, follow} and point their canonical at the calendar.
 * <p>
 * Pure: it reads the values it needs from {@link Settings} and the base url, so it can be unit tested without
 * a database.
 */
public record PageMeta(String title, String description, String canonicalUrl, String robots,
                       String ogImageUrl, String structuredData) {

    /** The two public views of the same agenda. */
    public enum View {
        CALENDAR("dance calendar"),
        LIST("dance events");

        private final String noun;

        View(String noun) {
            this.noun = noun;
        }
    }

    /** What fits in a search result; anything longer is cut at a word. */
    private static final int DESCRIPTION_MAX = 155;

    /**
     * @param baseUrl the public address of this deployment, without a trailing slash
     * @param filtered whether the request carried a query string, i.e. the visitor is looking at a variant
     */
    public static PageMeta of(Settings settings, String baseUrl, View view, boolean filtered) {
        String siteTitle = text(settings.title(), "Where Shall We Dance?");
        String subtitle = text(settings.subtitle(), "Ballroom and latin");
        boolean noindex = view == View.LIST || filtered;
        boolean hasLogo = settings.logo() != null && settings.logo().length > 0;
        return new PageMeta(
                siteTitle + " – " + subtitle + " " + view.noun,
                description(subtitle, settings.disclaimer()),
                baseUrl + "/",
                noindex ? "noindex, follow" : null,
                hasLogo ? baseUrl + "/logo" : null,
                structuredData(siteTitle, baseUrl));
    }

    /** @return the subtitle plus as much of the disclaimer as a search result shows, cut at a word */
    private static String description(String subtitle, String disclaimer) {
        String text = (subtitle + ". " + text(disclaimer, "")).replaceAll("\\s+", " ").strip();
        if (text.length() <= DESCRIPTION_MAX) {
            return text;
        }
        int cut = text.lastIndexOf(' ', DESCRIPTION_MAX);
        return text.substring(0, cut > 0 ? cut : DESCRIPTION_MAX).strip() + "…";
    }

    /** What the site is, in the form a search engine and an assistant read: a website with a name. */
    private static String structuredData(String siteTitle, String baseUrl) {
        return "{"
                + "\"@context\":\"https://schema.org\","
                + "\"@type\":\"WebSite\","
                + "\"name\":\"" + json(siteTitle) + "\","
                + "\"url\":\"" + json(baseUrl + "/") + "\""
                + "}";
    }

    /** A literal {@code <} in a script element would end it; {@code \u003c} is the JSON escape for it. */
    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("<", "\\u003c");
    }

    private static String text(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.strip();
    }
}
