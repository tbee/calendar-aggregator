package nl.softworks.calendarAggregator.infrastructure.http;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Keeps the writer host out of the index.
 * <p>
 * The same application serves {@code where-admin.shallwedance.org} (where the locations and sources are
 * maintained) and {@code where.shallwedance.org} (the public agenda), with the same public pages on both. The
 * admin host is not content, so every response it serves carries {@code X-Robots-Tag: noindex, nofollow};
 * its robots.txt disallows everything as well (see {@link RobotsController}).
 * <p>
 * The decision is based on the configured {@code baseUrl}, not on the request: a deployment knows which host
 * it is, and the header then applies to every address, including the ones a crawler constructs itself.
 */
@Component
public class AdminHostNoIndexFilter extends OncePerRequestFilter {

    private final boolean adminHost;

    public AdminHostNoIndexFilter(@Value("${baseUrl:http://localhost:8080}") String baseUrl) {
        this.adminHost = baseUrl.contains("where-admin");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (adminHost) {
            response.setHeader("X-Robots-Tag", "noindex, nofollow");
        }
        filterChain.doFilter(request, response);
    }
}
