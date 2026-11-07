package nl.softworks.calendarAggregator.infrastructure.mcp;

import io.modelcontextprotocol.client.McpClient;
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
public class BrowserToolProviderImpl implements BrowserToolProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserToolProviderImpl.class);

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

    private final BlockingQueue<BrowserMcpSyncClient> availableBrowserMcpSyncClients = new LinkedBlockingQueue<>();
    private final Set<BrowserMcpSyncClient> allBrowserMcpSyncClients = ConcurrentHashMap.newKeySet();
    private Semaphore poolSizeSemaphore;

    @PostConstruct
    private void init() {
        poolSizeSemaphore = new Semaphore(Math.max(1, poolSize));
        LOGGER.info("MCP browser pool for {} with {} client(s)", playwrightUrl, Math.max(1, poolSize));
    }

    @Override
    public String url() {
        return playwrightUrl;
    }

    /**
     * Lease a client for one run.
     * Blocks when all clients are in use; always release the lease in a try-with-resources or a finally block.
     */
    @Override
    public Session lease(Set<String> allowedHosts, int maxToolCalls, Consumer<String> log) {

        // Check if the pool has a BrowserTool available
        try {
            poolSizeSemaphore.acquire();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for an MCP browser", e);
        }

        try {
            // Fetch an available client from the pool, or create a new one and add it to the poll
            BrowserMcpSyncClient client = availableBrowserMcpSyncClients.poll();
            if (client == null) {
                client = createClient();
                allBrowserMcpSyncClients.add(client);
            }

            // Create a session with the client
            List<GuardedMcpBrowserTool> tools = GuardedMcpBrowserTool.wrap(client.tools, maxToolCalls, allowedHosts, log);
            return new SessionImpl(client, tools);
        }
        catch (RuntimeException e) {
            poolSizeSemaphore.release();
            throw e;
        }
    }

    private BrowserMcpSyncClient createClient() {
        LOGGER.info("Connecting MCP client to {}", playwrightUrl);
        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport
                .builder(playwrightUrl)
                .endpoint("/mcp")
                .build();

        io.modelcontextprotocol.client.McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(requestTimeoutSeconds))
                .initializationTimeout(Duration.ofSeconds(20))
                .build();
        try {
            client.initialize();

            // Get all possible tools for this client and filter for the allowed ones
            List<ToolCallback> allowedTools = McpToolUtils.getToolCallbacksFromSyncClients(client)
                    .stream()
                    .filter(tool -> ALLOWED_TOOL_NAMES.contains(tool.getToolDefinition().name()))
                    .toList();
            if (allowedTools.isEmpty()) {
                throw new IllegalStateException("The MCP browser at " + playwrightUrl + " exposes none of the expected tools " + ALLOWED_TOOL_NAMES);
            }
            LOGGER.info("MCP client connected, tools: {}", allowedTools.stream().map(tool -> tool.getToolDefinition().name()).toList());
            return new BrowserMcpSyncClient(client, allowedTools);
        }
        catch (RuntimeException e) {
            closeQuietly(client);
            throw e;
        }
    }

    private void release(BrowserMcpSyncClient client) {
        availableBrowserMcpSyncClients.offer(client);
        poolSizeSemaphore.release();
    }

    private void invalidate(BrowserMcpSyncClient client) {
        allBrowserMcpSyncClients.remove(client);
        closeQuietly(client.client);
        poolSizeSemaphore.release();
    }

    private void closeQuietly(io.modelcontextprotocol.client.McpSyncClient client) {
        try {
            client.close();
        }
        catch (RuntimeException e) {
            LOGGER.debug("Error closing MCP client", e);
        }
    }

    @PreDestroy
    public void close() {
        for (BrowserMcpSyncClient client : allBrowserMcpSyncClients) {
            closeQuietly(client.client);
        }
        allBrowserMcpSyncClients.clear();
        availableBrowserMcpSyncClients.clear();
    }

    private static final class BrowserMcpSyncClient {
        private final io.modelcontextprotocol.client.McpSyncClient client;
        private final List<ToolCallback> tools;

        private BrowserMcpSyncClient(io.modelcontextprotocol.client.McpSyncClient client, List<ToolCallback> tools) {
            this.client = client;
            this.tools = tools;
        }
    }

    /** One client, borrowed for one run. Closing returns it to the pool; invalidating discards it. */
    private final class SessionImpl implements BrowserToolProvider.Session {
        private final BrowserMcpSyncClient client;
        private final List<? extends ToolCallback> tools;
        private final AtomicBoolean finished = new AtomicBoolean();

        private SessionImpl(BrowserMcpSyncClient client, List<? extends ToolCallback> tools) {
            this.client = client;
            this.tools = tools;
        }

        @Override
        public List<? extends ToolCallback> tools() {
            return tools;
        }

        /**
         * Throw the client away instead of reusing it, e.g. after a transport error or when
         * the MCP server was restarted. The next lease creates a fresh session.
         */
        @Override
        public void invalidate() {
            if (finished.compareAndSet(false, true)) {
                BrowserToolProviderImpl.this.invalidate(client);
            }
        }

        @Override
        public void close() {
            if (finished.compareAndSet(false, true)) {
                BrowserToolProviderImpl.this.release(client);
            }
        }
    }
}