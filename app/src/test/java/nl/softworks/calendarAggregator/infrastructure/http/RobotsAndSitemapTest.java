package nl.softworks.calendarAggregator.infrastructure.http;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * robots.txt and sitemap.xml: generated per deployment, because the same application serves the public host
 * and the writer host.
 */
class RobotsAndSitemapTest {

    private static final String PUBLIC = "https://where.shallwedance.org";
    private static final String ADMIN = "https://where-admin.shallwedance.org";

    private MockMvc mockMvc(String baseUrl) {
        return MockMvcBuilders.standaloneSetup(new RobotsController(baseUrl), new SitemapController(baseUrl)).build();
    }

    @Test
    void thePublicHostAllowsCrawlingAndNamesItsSitemap() throws Exception {
        mockMvc(PUBLIC).perform(get("/robots.txt"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User-agent: *")))
                .andExpect(content().string(containsString("Allow: /")))
                .andExpect(content().string(containsString("Sitemap: " + PUBLIC + "/sitemap.xml")));
    }

    @Test
    void theAdminHostIsNotIndexedAtAll() throws Exception {
        mockMvc(ADMIN).perform(get("/robots.txt"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Disallow: /")))
                .andExpect(content().string(not(containsString("Allow: /"))))
                .andExpect(content().string(not(containsString("Sitemap:"))));
    }

    @Test
    void theSitemapListsTheCalendarAndNothingThatIsNoIndexed() throws Exception {
        mockMvc(PUBLIC).perform(get("/sitemap.xml"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/xml"))
                .andExpect(content().string(containsString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")))
                .andExpect(content().string(containsString("<loc>" + PUBLIC + "/</loc>")))
                // the list view carries noindex, so listing it would be a contradiction
                .andExpect(content().string(not(containsString("/list"))));
    }

    @Test
    void aTrailingSlashInTheConfiguredBaseUrlDoesNotDoubleUp() throws Exception {
        mockMvc(PUBLIC + "/").perform(get("/sitemap.xml"))
                .andExpect(content().string(containsString("<loc>" + PUBLIC + "/</loc>")));
    }
}
