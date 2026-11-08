package nl.softworks.calendarAggregator.infrastructure.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * The sitemap of the site: the pages a search engine should know about.
 * <p>
 * That is one page for now, the calendar on {@code /}. The list view shows the same events in another form and
 * every filter creates a variant, so those are {@code noindex} (see {@link PageMeta}) and are not listed. When
 * the calendar gets a page per location and per event (see TODO.md, phase 2), they are added here - with a
 * {@code <lastmod>} once the events carry a timestamp.
 */
@Controller
public class SitemapController {

    private final String baseUrl;

    public SitemapController(@Value("${baseUrl:http://localhost:8080}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemap() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n"
                + "  <url><loc>" + escape(baseUrl + "/") + "</loc></url>\n"
                + "</urlset>\n";
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
