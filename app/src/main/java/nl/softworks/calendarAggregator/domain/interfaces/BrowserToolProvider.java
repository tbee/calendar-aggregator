package nl.softworks.calendarAggregator.domain.interfaces;

import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Provides browser tools for one AI-agent run. Implemented in the infrastructure layer
 * (Playwright MCP), so the domain does not depend on that implementation.
 */
public interface BrowserToolProvider {

    /** Url of the browser server, for logging. */
    String url();

    /**
     * Lease an isolated browser session for one run. Blocks when all sessions are in use.
     *
     * @param allowedHosts  hosts the agent may open; matching ignores case and a leading "www."
     * @param maxToolCalls  hard budget for tool calls in this run
     * @param log           receives one line per tool interaction
     */
    Session lease(Set<String> allowedHosts, int maxToolCalls, Consumer<String> log);

    /** One exclusive session for one run; always close it. */
    interface Session extends AutoCloseable {

        /** Tool callbacks to hand to the chat client, already budgeted, guarded and logged. */
        List<ToolCallback> tools();

        /** Discard the underlying client instead of reusing it, e.g. after a transport error. */
        void invalidate();

        @Override
        void close();
    }
}