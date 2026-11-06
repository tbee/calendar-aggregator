package nl.softworks.calendarAggregator.infrastructure.mcp;

import nl.softworks.calendarAggregator.domain.interfaces.BrowserToolProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end check of the MCP plumbing without an LLM: connects to a real Playwright MCP
 * container, leases a session, navigates and reads a snapshot through the tool wrapper.
 *
 * <p>Runs only on request, because it needs Docker and the Playwright MCP image:
 * {@code mvnw -pl app test -Dtest=McpBrowserPoolIT -Dmcp.it=true}</p>
 */
@EnabledIfSystemProperty(named = "mcp.it", matches = "true")
class McpBrowserPoolIT {

    private static GenericContainer<?> playwright;
    private static McpBrowserPool pool;

    @BeforeAll
    static void start() {
        playwright = new GenericContainer<>(DockerImageName.parse("mcr.microsoft.com/playwright/mcp:v0.0.83"))
                .withExposedPorts(8931)
                .withCommand("--isolated", "--port", "8931", "--host", "0.0.0.0", "--allowed-hosts", "*", "--snapshot-mode", "none", "--idle-timeout", "60000")
                .withSharedMemorySize(1024L * 1024 * 1024)
                .waitingFor(Wait.forLogMessage(".*Listening on.*", 1))
                .withStartupTimeout(Duration.ofMinutes(10));
        playwright.start();

        pool = new McpBrowserPool();
        ReflectionTestUtils.setField(pool, "playwrightUrl", "http://localhost:" + playwright.getMappedPort(8931));
        ReflectionTestUtils.setField(pool, "poolSize", 2);
        ReflectionTestUtils.setField(pool, "requestTimeoutSeconds", 120L);
        ReflectionTestUtils.invokeMethod(pool, "init");
    }

    @AfterAll
    static void stop() {
        if (pool != null) {
            pool.close();
        }
        if (playwright != null) {
            playwright.stop();
        }
    }

    @Test
    void browsesAndSnapshots() {
        try (BrowserToolProvider.Session session = pool.lease(Set.of("example.com"), 10, System.out::println)) {
            ToolCallback navigate = byName(session.tools(), "browser_navigate");
            ToolCallback snapshot = byName(session.tools(), "browser_snapshot");

            String nav = navigate.call("{\"url\":\"https://example.com\"}");
            System.out.println(nav);
            assertTrue(nav.contains("Example Domain"), "Unexpected navigate result: " + nav);
            assertTrue(nav.contains("Page URL"), "Unexpected navigate result: " + nav);

            String snap = snapshot.call("{}");
            System.out.println(snap);
            assertTrue(snap.toLowerCase().contains("example domain"), "Unexpected snapshot result: " + snap);
            assertFalse(snap.startsWith("["), "The MCP content array should have been unwrapped: " + snap);
        }
    }

    private static ToolCallback byName(List<ToolCallback> tools, String name) {
        return tools.stream()
                .filter(tool -> tool.getToolDefinition().name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError(name + " not exposed, tools: " + tools.stream()
                        .map(tool -> tool.getToolDefinition().name()).toList()));
    }
}