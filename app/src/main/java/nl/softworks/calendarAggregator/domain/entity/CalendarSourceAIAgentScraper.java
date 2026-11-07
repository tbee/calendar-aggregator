package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import nl.softworks.calendarAggregator.domain.interfaces.BrowserToolProvider;
import nl.softworks.calendarAggregator.domain.interfaces.S;
import nl.softworks.calendarAggregator.domain.service.CalendarEventsXmlParser;
import nl.softworks.calendarAggregator.domain.service.OpenRouterService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Calendar source that hands a prompt with one or more URLs to an LLM and lets the model
 * drive a real browser (via the Playwright MCP server) to find the events.
 */
@Entity
@DiscriminatorValue("aiagent")
public class CalendarSourceAIAgentScraper extends CalendarSource {

    /** Hard limit on browser tool calls per run; the wrapper refuses calls after this. */
    private static final int MAX_TOOL_CALLS = 30;
    private static final Pattern URL_PATTERN = Pattern.compile("https?://[^\\s\"'<>()\\[\\]]+");

    public String type() {
        return "AIAgent";
    }

    @NotNull
    @Column(name = "subject")
    private String model = OpenRouterService.DEFAULT_AGENT_MODEL;
    static public final String MODEL = "model";
    public String model() {
        return model;
    }
    public CalendarSourceAIAgentScraper model(String v) {
        this.model = v;
        return this;
    }

    @NotNull
    @Column(name = "regex")
    private String prompt;
    static public final String PROMPT = "prompt";
    public String prompt() {
        return prompt;
    }
    public CalendarSourceAIAgentScraper prompt(String v) {
        this.prompt = v;
        return this;
    }

    @Override
    public Set<CalendarEvent> generateEvents() {
        try {
            super.generateEvents();
            if (!isEnabled()) {
                return calendarEvents;
            }

            Set<String> allowedHosts = extractAllowedHosts();
            if (allowedHosts.isEmpty()) {
                status("No URL in prompt");
                logAppend("The prompt does not contain an http(s) URL, so there is nothing to browse\n");
                return Set.of();
            }

            String timezone = determineTimezone() == null ? "" : determineTimezone().name();
            LocalDate today = localDateTimeNowSupplier.get().toLocalDate();
            String systemPrompt = """
                    You are a web scraper that extracts dancing events from websites.
                    You have a real browser and can read and navigate websites.

                    Work like this:
                    1. Open each URL from the assignment with browser_navigate.
                    2. browser_navigate only reports the page URL and title; call browser_snapshot to read the page itself.
                    3. Use the snapshot to find the agenda, calendar or events page, and click the link (use the [ref] from the latest snapshot).
                    4. A [ref] is only valid in the snapshot it came from. After every browser_navigate or browser_click the page has changed: call browser_snapshot again before you use a [ref].
                    5. When a tool call fails with an error such as "Ref ... not found in the current page snapshot", call browser_snapshot and retry with a [ref] from that new snapshot.
                    6. Open event detail pages when the overview does not show the dates or times.
                    7. Use browser_find when you only need to locate something; it is cheaper than reading a whole snapshot.
                    8. If events are only shown as a flyer or poster image, call inspectFlyers while on that page. It reads the image and adds the events itself.
                    9. Stay on these hosts: %s. Do not open pages on other hosts.
                    10. Use at most %d browser tool calls. When you have enough information, stop calling tools and return the XML.

                    Today is %s (%s). Resolve dates without a year against today, and never return events in the past.
                    If events do not contain a time, make a reasonable assumption. For example: events in the evening usually start at 20:00 and end around 23:00.
                    Ignore events that are about getting people to start taking lessons, like "kennismakingsles" of "proefles", or the start of a new season or block of lessons.
                    Ignore events that actually are indicators there are no lessons ("Geen les / Geen lessen").
                    
                    Return the events in exactly this XML structure:
                    <events>
                        <event>
                            <startDate>2024-01-01</startDate>
                            <endDate>2024-01-02</endDate>
                            <startTime>10:00</startTime>
                            <endTime>12:00</endTime>
                            <description>Dancing is fun</description>
                        </event>
                    </events>
                    Dates must be in the format yyyy-MM-dd.
                    Times must be in the format HH:mm.
                    Only return the XML structure, nothing else.
                    """.formatted(String.join(", ", allowedHosts), MAX_TOOL_CALLS, today, timezone);
            String userPrompt = (prompt == null ? "" : prompt);
            logAppend(systemPrompt + "\n" + userPrompt + "\n");

            String selectedModel = (model == null || model.isBlank()) ? OpenRouterService.DEFAULT_AGENT_MODEL : model;
            if (!modelSupportsToolCalling(selectedModel)) {
                status("Model does not support tool calling: " + selectedModel);
                logAppend("Model " + selectedModel + " does not advertise tool calling, so it cannot drive the browser\n");
                return Set.of();
            }
            logAppend("Model: " + selectedModel + "\n");
            logAppend("Browser: " + S.browserToolProvider().url() + "\n");

            BrowserToolProvider.RunContext runContext = new BrowserToolProvider.RunContext(
                    allowedHosts, MAX_TOOL_CALLS, this::logAppend, this::addCalendarEvent, today, timezone);
            BrowserToolProvider.Session session = S.browserToolProvider().lease(runContext);
            ChatResponse chatResponse;
            try {
                List<? extends ToolCallback> tools = session.tools();
                logAppend("Tools: " + tools.stream().map(tool -> tool.getToolDefinition().name()).collect(Collectors.joining(", ")) + "\n");

                chatResponse = ChatClient
                        .create(S.chatModel())
                        .prompt()
                        .options(OpenAiChatOptions.builder().model(selectedModel))
                        .system(systemPrompt)
                        .user(userPrompt)
                        .tools(tools)
                        .call()
                        .chatResponse();
            }
            catch (RuntimeException e) {
                // The MCP session may be broken (e.g. the browser server restarted); do not reuse it.
                session.invalidate();
                throw e;
            }
            finally {
                session.close();
            }

            String response = chatResponse == null ? null : chatResponse.getResult().getOutput().getText();
            logAppend("Resolved model: " + (chatResponse == null ? "unknown" : chatResponse.getMetadata().getModel()) + "\n");
            logAppend(response + "\n");
            for (CalendarEvent calendarEvent : CalendarEventsXmlParser.parse(response, this::logAppend)) {
                addCalendarEvent(calendarEvent);
                logAppend("Added event: " + calendarEvent + "\n");
            }

            dedupeEvents();
            dropExpiredEvents();
            sanatizeEvents();
            logAppend("Done\n");

            // set status
            if (calendarEvents().isEmpty()) {
                status("No events");
                return Set.of();
            }

            return calendarEvents();
        }
        catch (RuntimeException e) {
            status(e.getMessage());
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            logAppend(stringWriter.toString());
            throw e;
        }
    }

    // flyer events may repeat text events; keep one per start moment + subject
    private void dedupeEvents() {
        Map<String, CalendarEvent> unique = new LinkedHashMap<>();
        for (CalendarEvent event : calendarEvents) {
            unique.putIfAbsent(event.startDateTime() + "|" + event.subject().trim().toLowerCase(Locale.ROOT), event);
        }
        if (unique.size() != calendarEvents.size()) {
            int removed = calendarEvents.size() - unique.size();
            calendarEvents.clear();
            calendarEvents.addAll(unique.values());
            logAppend("Removed " + removed + " duplicate events\n");
        }
    }

    /**
     * Returns false only when the model is known and does not advertise tool calling.
     * An unknown model is allowed, because the model list may be unavailable; the call
     * itself will show whether the model can use the tools.
     */
    private boolean modelSupportsToolCalling(String modelId) {
        return S.openRouterService().models().stream()
                .filter(model -> model.id().equals(modelId))
                .findFirst()
                .map(OpenRouterService.Model::supportsTools)
                .orElse(true);
    }

    /**
     * The prompt contains the seed URLs; their hosts are the only ones the agent may open.
     */
    private Set<String> extractAllowedHosts() {
        Set<String> hosts = new HashSet<>();
        Matcher matcher = URL_PATTERN.matcher(prompt == null ? "" : prompt);
        while (matcher.find()) {
            String url = matcher.group().replaceAll("[.,;:]+$", "");
            try {
                String host = URI.create(url).getHost();
                if (host != null && !host.isBlank()) {
                    hosts.add(host.toLowerCase(Locale.ROOT));
                }
            }
            catch (IllegalArgumentException e) {
                logAppend("Ignoring invalid URL in prompt: " + url + "\n");
            }
        }
        return hosts;
    }
}