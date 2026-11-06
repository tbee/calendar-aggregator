package nl.softworks.calendarAggregator.infrastructure.mcp;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.util.JsonHelper;
import org.springframework.core.ParameterizedTypeReference;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Decorates MCP browser tool callbacks with the guardrails an unattended agent needs:
 * a hard call budget, a log of every interaction (written to the CalendarSource log),
 * and a host check on URLs the model asks the browser to open.
 *
 * <p>The budget and the log are shared by all callbacks of a single run, so the model
 * cannot get around the limit by switching tools.</p>
 */
final class McpBrowserToolWrapper implements ToolCallback {

    private static final Pattern URL_IN_ARGUMENTS = Pattern.compile("\"url\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");

    private static final ParameterizedTypeReference<List<Map<String, Object>>> MCP_CONTENT =
            new ParameterizedTypeReference<>() {
            };
    private static final JsonHelper JSON_HELPER = new JsonHelper();

    private static final int LOG_ARGUMENTS_MAX = 1_000;
    private static final int LOG_RESULT_MAX = 4_000;

    private final ToolCallback delegate;
    private final AtomicInteger remaining;
    private final int maxCalls;
    private final Set<String> allowedHosts;
    private final Consumer<String> log;
    private final String toolName;

    private McpBrowserToolWrapper(ToolCallback delegate, AtomicInteger remaining, int maxCalls, Set<String> allowedHosts, Consumer<String> log) {
        this.delegate = delegate;
        this.remaining = remaining;
        this.maxCalls = maxCalls;
        this.allowedHosts = allowedHosts;
        this.log = log;
        this.toolName = delegate.getToolDefinition().name();
    }

    static List<ToolCallback> wrap(List<ToolCallback> tools, int maxCalls, Set<String> allowedHosts, Consumer<String> log) {
        AtomicInteger remaining = new AtomicInteger(maxCalls);
        List<ToolCallback> wrapped = new ArrayList<>(tools.size());
        for (ToolCallback tool : tools) {
            wrapped.add(new McpBrowserToolWrapper(tool, remaining, maxCalls, allowedHosts, log));
        }
        return wrapped;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
    }

    @Override
    public String call(String toolInput) {
        return call(toolInput, null);
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        int remainingAfterThisCall = remaining.getAndDecrement();
        if (remainingAfterThisCall <= 0) {
            remaining.set(0);
            String message = "Tool budget of " + maxCalls + " calls is exhausted. "
                    + "Do not call any more tools, return the final XML with the events found so far.";
            log.accept("Tool budget exhausted\n");
            return message;
        }
        int callNumber = maxCalls - remainingAfterThisCall + 1;

        String blocked = checkHost(toolInput);
        if (blocked != null) {
            log.accept("Tool " + callNumber + "/" + maxCalls + " " + toolName + " blocked: " + blocked + "\n");
            return blocked;
        }

        log.accept("Tool " + callNumber + "/" + maxCalls + " " + toolName + " " + abbreviate(toolInput, LOG_ARGUMENTS_MAX) + "\n");
        long start = System.nanoTime();
        try {
            String result = unwrapMcpContent((toolContext == null) ? delegate.call(toolInput) : delegate.call(toolInput, toolContext));
            long millis = (System.nanoTime() - start) / 1_000_000;
            log.accept("Tool " + callNumber + " result (" + millis + " ms, "
                    + (result == null ? 0 : result.length()) + " chars): "
                    + abbreviate(result, LOG_RESULT_MAX) + "\n");
            return result;
        }
        catch (RuntimeException e) {
            long millis = (System.nanoTime() - start) / 1_000_000;
            log.accept("Tool " + callNumber + " failed after " + millis + " ms: " + e.getMessage() + "\n");
            throw e;
        }
    }

    /**
     * Returns an error message when the tool input contains a URL outside the allowed
     * hosts, or null when the call may proceed. Tools without a url argument (clicks,
     * snapshots) are not checked: a click can only be judged after the fact.
     */
    private String checkHost(String toolInput) {
        if (toolInput == null || allowedHosts.isEmpty()) {
            return null;
        }
        Matcher matcher = URL_IN_ARGUMENTS.matcher(toolInput);
        if (!matcher.find()) {
            return null;
        }
        String url = matcher.group(1);
        try {
            String host = URI.create(url.replace("\\/", "/")).getHost();
            if (host == null || host.isBlank()) {
                return "Not an absolute URL: " + url;
            }
            if (!allowedHosts.contains(normalizeHost(host))) {
                return "Blocked " + url + ": only pages on " + allowedHosts + " may be opened.";
            }
            return null;
        }
        catch (IllegalArgumentException e) {
            return "Invalid URL: " + url;
        }
    }

    static String normalizeHost(String host) {
        String normalized = host.toLowerCase(Locale.ROOT);
        return normalized.startsWith("www.") ? normalized.substring(4) : normalized;
    }

    /**
     * Spring AI returns the MCP result as the JSON representation of the content array
     * ({@code [{"type":"text","text":"..."}]}). The model reads plain text much better,
     * and the JSON escaping doubles the size of snapshots, so unwrap it here.
     */
    private static String unwrapMcpContent(String result) {
        if (result == null || result.isBlank() || result.charAt(0) != '[') {
            return result;
        }
        try {
            List<Map<String, Object>> parts = JSON_HELPER.fromJson(result, MCP_CONTENT);
            StringBuilder text = new StringBuilder();
            for (Map<String, Object> part : parts) {
                Object value = part.get("text");
                if (value == null) {
                    continue;
                }
                if (!text.isEmpty()) {
                    text.append('\n');
                }
                text.append(value);
            }
            return text.isEmpty() ? result : text.toString();
        }
        catch (RuntimeException e) {
            return result;
        }
    }

    private static String abbreviate(String s, int max) {
        if (s == null) {
            return "null";
        }
        if (s.length() <= max) {
            return s;
        }
        int head = max / 2;
        int tail = max - head;
        return s.substring(0, head)
                + "\n... (" + (s.length() - max) + " chars omitted) ...\n"
                + s.substring(s.length() - tail);
    }
}