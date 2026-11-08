package nl.softworks.calendarAggregator.infrastructure.http;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** The writer host serves the same public pages as the reader host, but must never end up in an index. */
class AdminHostNoIndexFilterTest {

    private static MockHttpServletResponse filtered(String baseUrl) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        new AdminHostNoIndexFilter(baseUrl).doFilter(new MockHttpServletRequest(), response, new MockFilterChain());
        return response;
    }

    @Test
    void theAdminHostCarriesTheNoIndexHeaderOnEveryResponse() throws Exception {
        assertEquals("noindex, nofollow", filtered("https://where-admin.shallwedance.org").getHeader("X-Robots-Tag"));
    }

    @Test
    void thePublicHostIsLeftAlone() throws Exception {
        assertNull(filtered("https://where.shallwedance.org").getHeader("X-Robots-Tag"));
    }
}
