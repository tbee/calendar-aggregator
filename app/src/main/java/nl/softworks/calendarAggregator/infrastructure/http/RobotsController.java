package nl.softworks.calendarAggregator.infrastructure.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.nio.charset.StandardCharsets;

/**
 * The robots.txt of this deployment.
 * <p>
 * Generated rather than a static file, because the same application serves two hosts: the public reader
 * ({@code where.shallwedance.org}) allows everything and points at its sitemap, while the writer host
 * ({@code where-admin…}) serves the same public pages and must never be indexed, so it disallows everything.
 * <p>
 * Nothing is disallowed on the public host: the pages that may not be indexed (the filtered variants, the
 * list view) carry a {@code noindex} themselves, and a crawler that is refused permission to fetch a page
 * cannot see that directive.
 */
@Controller
public class RobotsController {

    private final String baseUrl;
    private final boolean adminHost;

    public RobotsController(@Value("${baseUrl:http://localhost:8080}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.adminHost = this.baseUrl.contains("where-admin");
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> robots() {
        String body = adminHost
                ? "User-agent: *\nDisallow: /\n"
                : "User-agent: *\nAllow: /\n\nSitemap: " + baseUrl + "/sitemap.xml\n";
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                .body(body);
    }
}
