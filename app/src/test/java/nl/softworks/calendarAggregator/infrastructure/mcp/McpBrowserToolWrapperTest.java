package nl.softworks.calendarAggregator.infrastructure.mcp;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class McpBrowserToolWrapperTest {

    private final List<String> log = new ArrayList<>();

    @Test
    void stopsAfterMaxCalls() {
        AtomicInteger calls = new AtomicInteger();
        ToolCallback delegate = callback("browser_navigate", input -> {
            calls.incrementAndGet();
            return "snapshot";
        });

        List<ToolCallback> wrapped = McpBrowserToolWrapper.wrap(List.of(delegate), 2, Set.of(), log::add);

        assertEquals("snapshot", wrapped.get(0).call("{\"url\":\"https://example.com\"}"));
        assertEquals("snapshot", wrapped.get(0).call("{\"url\":\"https://example.com/2\"}"));
        String third = wrapped.get(0).call("{\"url\":\"https://example.com/3\"}");

        assertTrue(third.contains("budget"));
        assertEquals(2, calls.get());
        assertTrue(log.stream().anyMatch(line -> line.contains("Tool budget exhausted")));
    }

    @Test
    void blocksOtherHosts() {
        AtomicReference<String> lastInput = new AtomicReference<>();
        ToolCallback delegate = callback("browser_navigate", input -> {
            lastInput.set(input);
            return "snapshot";
        });

        List<ToolCallback> wrapped = McpBrowserToolWrapper.wrap(List.of(delegate), 5, Set.of("example.com"), log::add);

        String blocked = wrapped.get(0).call("{\"url\":\"https://evil.example.net/events\"}");

        assertTrue(blocked.contains("Blocked"));
        verify(delegate, never()).call(anyString());
        assertTrue(log.stream().anyMatch(line -> line.contains("blocked")));

        // www. is normalized away, so this one is allowed
        assertEquals("snapshot", wrapped.get(0).call("{\"url\":\"https://www.example.com/events\"}"));
        assertTrue(lastInput.get().contains("www.example.com"));
    }

    @Test
    void logsCallAndResult() {
        ToolCallback delegate = callback("browser_snapshot", input -> "### Page\n- Page URL: https://example.com");

        List<ToolCallback> wrapped = McpBrowserToolWrapper.wrap(List.of(delegate), 5, Set.of("example.com"), log::add);
        wrapped.get(0).call("{}");

        String logText = String.join("", log);
        assertTrue(logText.contains("Tool 1/5 browser_snapshot"));
        assertTrue(logText.contains("Page URL"));
    }

    @Test
    void toolsWithoutUrlAreAllowed() {
        ToolCallback delegate = callback("browser_snapshot", input -> "snapshot");

        List<ToolCallback> wrapped = McpBrowserToolWrapper.wrap(List.of(delegate), 5, Set.of("example.com"), log::add);

        assertEquals("snapshot", wrapped.get(0).call("{}"));
        verify(delegate, times(1)).call(anyString());
        assertFalse(log.isEmpty());
    }

    @Test
    void unwrapsMcpContentArray() {
        ToolCallback delegate = callback("browser_snapshot",
                input -> "[{\"type\":\"text\",\"text\":\"### Page\\n- Page URL: https://example.com\"}]");

        List<ToolCallback> wrapped = McpBrowserToolWrapper.wrap(List.of(delegate), 5, Set.of("example.com"), log::add);

        assertEquals("### Page\n- Page URL: https://example.com", wrapped.get(0).call("{}"));
    }

    private static ToolCallback callback(String name, java.util.function.Function<String, String> handler) {
        ToolCallback delegate = mock(ToolCallback.class);
        when(delegate.getToolDefinition()).thenReturn(ToolDefinition.builder()
                .name(name)
                .description("test tool")
                .inputSchema("{}")
                .build());
        when(delegate.call(anyString())).thenAnswer(invocation -> handler.apply(invocation.getArgument(0)));
        return delegate;
    }
}