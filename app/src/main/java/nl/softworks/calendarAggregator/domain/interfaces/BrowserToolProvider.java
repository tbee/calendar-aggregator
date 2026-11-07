package nl.softworks.calendarAggregator.domain.interfaces;

import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import org.springframework.ai.tool.ToolCallback;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Provides browser tools for one AI-agent run. Implemented in the infrastructure layer
 * (Playwright MCP), so the domain does not depend on that implementation.
 */
public interface BrowserToolProvider {

    // everything one run needs; the sink receives events the tools extract themselves
    record RunContext(Set<String> allowedHosts, int maxToolCalls, Consumer<String> log,
                      Consumer<CalendarEvent> eventSink, LocalDate today, String timezone) {
    }

    /** Url of the browser server, for logging. */
    String url();

    /** Lease an isolated browser session for one run. Blocks when all sessions are in use. */
    Session lease(RunContext context);

    /** One exclusive session for one run; always close it. */
    interface Session extends AutoCloseable {

        /** Tool callbacks to hand to the chat client, already budgeted, guarded and logged. */
        List<? extends ToolCallback> tools();

        /** Discard the underlying client instead of reusing it, e.g. after a transport error. */
        void invalidate();

        @Override
        void close();
    }
}