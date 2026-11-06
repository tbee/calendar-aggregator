package nl.softworks.calendarAggregator.infrastructure.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import nl.softworks.calendarAggregator.domain.interfaces.BrowserToolProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Playwright MCP implementation of {@link BrowserToolProvider}.
 *
 * <p>Every client is a separate MCP session, and the server (started with {@code --isolated})
 * gives every session its own browser context. A session is leased for the duration of one
 * CalendarSource run, so concurrent runs never share a browser page.</p>
 *
 * <p>The server is not contacted until the first lease is taken: the Playwright MCP server
 * creates the backend (and therefore the browser context) on the first tool call, so holding
 * idle clients costs nothing.</p>
 */
@Service
public class McpBrowserPool implements BrowserToolProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(McpBrowserPool.class);

    /** Tools the agent is allowed to use; anything else (vision, pdf, devtools, code execution) is hidden from the model. */
    private static final Set<String> ALLOWED_TOOL_NAMES = Set.of(
            "browser_navigate",
            "browser_snapshot",
            "browser_find",
            "browser_click",
            "browser_wait_for",
            "browser_tabs"
    );

    @Value("${calendaraggregator.mcp.playwright.url:http://localhost:8931}")
    private String playwrightUrl;

    @Value("${calendaraggregator.aiAgent.poolSize:3}")
    private int poolSize;

    @Value("${calendaraggregator.mcp.requestTimeoutSeconds:120}")
    private long requestTimeoutSeconds;

    private final BlockingQueue<PooledClient> available = new LinkedBlockingQueue<>();
    private final Set<PooledClient> all = ConcurrentHashMap.newKeySet();
    private Semaphore permits;

    @PostConstruct
    private void init() {
        permits = new Semaphore(Math.max(1, poolSize));
        LOGGER.info("MCP browser pool for {} with {} client(s)", playwrightUrl, Math.max(1, poolSize));
    }

    @Override
    public String url() {
        return playwrightUrl;
    }

    /**
     * Lease a client for one run. Blocks when all clients are in use; always release the
     * lease in a try-with-resources or a finally block.
     */
    @Override
    public Session lease(Set<String> allowedHosts, int maxToolCalls, Consumer<String> log) {
        try {
            permits.acquire();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for an MCP browser", e);
        }

        try {
            PooledClient client = available.poll();
            if (client == null) {
                client = createClient();
                all.add(client);
            }
            Set<String> normalizedHosts = allowedHosts.stream()
                    .map(McpToolWrapper::normalizeHost)
                    .collect(Collectors.toSet());
            List<ToolCallback> tools = McpToolWrapper.wrap(client.tools, maxToolCalls, normalizedHosts, log);
            return new Lease(client, tools);
        }
        catch (RuntimeException e) {
            permits.release();
            throw e;
        }
    }

    private PooledClient createClient() {
        LOGGER.info("Connecting MCP client to {}", playwrightUrl);
        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport
                .builder(playwrightUrl)
                .endpoint("/mcp")
                .build();

        McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(requestTimeoutSeconds))
                .initializationTimeout(Duration.ofSeconds(20))
                .build();
        try {
            client.initialize();

            List<ToolCallback> tools = McpToolUtils.getToolCallbacksFromSyncClients(client)
                    .stream()
                    .filter(tool -> ALLOWED_TOOL_NAMES.contains(tool.getToolDefinition().name()))
                    .toList();
            if (tools.isEmpty()) {
                throw new IllegalStateException("The MCP browser at " + playwrightUrl
                        + " exposes none of the expected tools " + ALLOWED_TOOL_NAMES);
            }
            LOGGER.info("MCP client connected, tools: {}", tools.stream().map(tool -> tool.getToolDefinition().name()).toList());
            return new PooledClient(client, tools);
        }
        catch (RuntimeException e) {
            closeQuietly(client);
            throw e;
        }
    }

    private void release(PooledClient client) {
        available.offer(client);
        permits.release();
    }

    private void invalidate(PooledClient client) {
        all.remove(client);
        closeQuietly(client.client);
        permits.release();
    }

    private void closeQuietly(McpSyncClient client) {
        try {
            client.close();
        }
        catch (RuntimeException e) {
            LOGGER.debug("Error closing MCP client", e);
        }
    }

    @PreDestroy
    public void close() {
        for (PooledClient client : all) {
            closeQuietly(client.client);
        }
        all.clear();
        available.clear();
    }

    private static final class PooledClient {
        private final McpSyncClient client;
        private final List<ToolCallback> tools;

        private PooledClient(McpSyncClient client, List<ToolCallback> tools) {
            this.client = client;
            this.tools = tools;
        }
    }

    /** One client, borrowed for one run. Closing returns it to the pool; invalidating discards it. */
    private final class Lease implements BrowserToolProvider.Session {
        private final PooledClient client;
        private final List<ToolCallback> tools;
        private final AtomicBoolean finished = new AtomicBoolean();

        private Lease(PooledClient client, List<ToolCallback> tools) {
            this.client = client;
            this.tools = tools;
        }

        @Override
        public List<ToolCallback> tools() {
            return tools;
        }

        /**
         * Throw the client away instead of reusing it, e.g. after a transport error or when
         * the MCP server was restarted. The next lease creates a fresh session.
         */
        @Override
        public void invalidate() {
            if (finished.compareAndSet(false, true)) {
                McpBrowserPool.this.invalidate(client);
            }
        }

        @Override
        public void close() {
            if (finished.compareAndSet(false, true)) {
                McpBrowserPool.this.release(client);
            }
        }
    }
}