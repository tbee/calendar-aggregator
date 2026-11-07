package nl.softworks.calendarAggregator.infrastructure.mcp;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.FlyerCache;
import nl.softworks.calendarAggregator.domain.interfaces.BrowserToolProvider;
import nl.softworks.calendarAggregator.domain.service.CalendarEventsXmlParser;
import nl.softworks.calendarAggregator.domain.service.FlyerCacheService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.util.JsonHelper;

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tool the model calls when a page shows its events as a flyer image. Discovery, download,
 * caching and the vision call all happen here; the model only sees a short status.
 */
public final class FlyerInspector implements ToolCallback {

    private static final JsonHelper JSON_HELPER = new JsonHelper();

    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder()
            .name("inspectFlyers")
            .description("Inspect poster or flyer images on the current page and extract events from them. "
                    + "Call this when events are only shown as an image. Takes no arguments.")
            .inputSchema("{\"type\":\"object\",\"properties\":{}}")
            .build();

    private static final String DISCOVERY_SCRIPT = """
            async () => {
              for (let i = 0; i < 4; i++) {
                window.scrollTo(0, document.body.scrollHeight);
                await new Promise(resolve => setTimeout(resolve, 600));
              }
              window.scrollTo(0, 0);
              const images = Array.from(document.images).map(img => ({
                src: img.currentSrc || img.src || img.getAttribute('data-src') || img.getAttribute('data-lazy-src') || '',
                w: img.naturalWidth || 0,
                h: img.naturalHeight || 0,
                alt: img.alt || ''
              }));
              const observed = performance.getEntriesByType('resource').map(entry => entry.name);
              return { pageUrl: location.href, pageTitle: document.title, images: images, observed: observed };
            }
            """;

    private static final String FETCH_SCRIPT = """
            async () => {
              const response = await fetch(location.href);
              const bytes = new Uint8Array(await response.arrayBuffer());
              let binary = '';
              for (let i = 0; i < bytes.length; i += 8192) {
                binary += String.fromCharCode.apply(null, bytes.subarray(i, i + 8192));
              }
              return { data: btoa(binary), mime: response.headers.get('content-type') || '' };
            }
            """;

    private final McpSyncClient client;
    private final BrowserToolProvider.RunContext context;
    private final FlyerImageFetcher imageFetcher;
    private final FlyerVisionReader visionReader;
    private final FlyerCacheService cacheService;
    private final int minWidth;
    private final int minHeight;
    private final int maxImages;

    public FlyerInspector(McpSyncClient client, BrowserToolProvider.RunContext context,
                          FlyerImageFetcher imageFetcher, FlyerVisionReader visionReader,
                          FlyerCacheService cacheService, int minWidth, int minHeight, int maxImages) {
        this.client = client;
        this.context = context;
        this.imageFetcher = imageFetcher;
        this.visionReader = visionReader;
        this.cacheService = cacheService;
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.maxImages = maxImages;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public String call(String toolInput) {
        try {
            return inspect();
        }
        catch (RuntimeException e) {
            context.log().accept("Flyer inspection failed: " + e + "\n");
            return "Flyer inspection failed: " + e.getMessage();
        }
    }

    private String inspect() {
        PageImages page = discover();
        if (page.images() == null || page.images().isEmpty()) {
            return "No images found on this page.";
        }

        Set<String> fetchHosts = context.allowedHosts().stream()
                .map(GuardedMcpBrowserTool::normalizeHost)
                .collect(Collectors.toSet());
        if (page.observed() != null) {
            page.observed().forEach(url -> addHost(fetchHosts, url));
        }
        addHost(fetchHosts, page.pageUrl());

        List<ImageInfo> candidates = filter(page.images(), page.pageUrl());
        if (candidates.isEmpty()) {
            return "No flyer-sized images found on this page.";
        }
        context.log().accept("Flyer candidates: " + candidates.size() + "\n");

        int visionCalls = 0;
        int cacheHits = 0;
        int addedEvents = 0;
        int skipped = 0;
        for (ImageInfo candidate : candidates) {
            context.log().accept("Flyer image " + candidate.src() + " (" + candidate.w() + "x" + candidate.h() + ")\n");
            if (!imageFetcher.isAllowed(candidate.src(), fetchHosts)) {
                context.log().accept("Flyer image host not allowed: " + candidate.src() + "\n");
                skipped++;
                continue;
            }
            FlyerImageFetcher.FetchedImage image = imageFetcher.fetch(candidate.src(), page.pageUrl(), fetchHosts).orElse(null);
            if (image == null) {
                context.log().accept("HTTP fetch failed, trying the browser\n");
                image = fetchViaBrowser(candidate.src());
            }
            if (image == null) {
                skipped++;
                continue;
            }

            String imageHash = sha256(image.bytes());
            Optional<FlyerCache> cached = cacheService.find(imageHash, visionReader.promptVersion(), visionReader.model());
            String raw;
            if (cached.isPresent() && cached.get().eventsXml() != null) {
                raw = cached.get().eventsXml();
                cacheService.touch(cached.get());
                cacheHits++;
                context.log().accept("Flyer cache hit " + imageHash + "\n");
            }
            else {
                raw = visionReader.read(image.bytes(), image.mimeType(), context.today(), context.timezone(), context.log());
                if (raw == null || raw.isBlank() || !raw.contains("<events")) {
                    skipped++;
                    continue;
                }
                List<CalendarEvent> events = CalendarEventsXmlParser.parse(raw, context.log());
                cacheService.store(imageHash, visionReader.promptVersion(), visionReader.model(),
                        candidate.src(), transcription(raw), raw, events);
                visionCalls++;
                addedEvents += addEvents(events);
                continue;
            }

            addedEvents += addEvents(CalendarEventsXmlParser.parse(raw, context.log()));
        }

        return "Flyer inspection: " + candidates.size() + " image(s), " + visionCalls + " read, "
                + cacheHits + " cached, " + addedEvents + " event(s) added"
                + (skipped > 0 ? ", " + skipped + " skipped" : "") + ".";
    }

    private int addEvents(List<CalendarEvent> events) {
        events.forEach(event -> context.eventSink().accept(event));
        return events.size();
    }

    private PageImages discover() {
        String text = callTool("browser_evaluate", Map.of("function", DISCOVERY_SCRIPT));
        return JSON_HELPER.fromJson(jsonSection(text), PageImages.class);
    }

    private List<ImageInfo> filter(List<ImageInfo> images, String pageUrl) {
        Set<String> seen = new HashSet<>();
        List<ImageInfo> candidates = new ArrayList<>();
        for (ImageInfo image : images) {
            String src = image.src();
            if (src == null || src.isBlank() || src.startsWith("data:") || src.startsWith("blob:")) {
                continue;
            }
            URI uri = resolve(pageUrl, src);
            if (uri == null || uri.getHost() == null
                    || (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))) {
                continue;
            }
            if (image.w() < minWidth || image.h() < minHeight) {
                continue;
            }
            String haystack = (src + " " + image.alt()).toLowerCase(Locale.ROOT);
            if (haystack.contains("logo") || haystack.contains("icon") || haystack.contains("sprite")
                    || haystack.contains("favicon") || haystack.contains("avatar") || haystack.contains("pixel")
                    || haystack.contains("tracking") || haystack.contains("social")) {
                continue;
            }
            if (!seen.add(uri.toString())) {
                continue;
            }
            candidates.add(new ImageInfo(uri.toString(), image.w(), image.h(), image.alt()));
        }
        candidates.sort(Comparator.comparingInt((ImageInfo image) -> image.w() * image.h()).reversed());
        return candidates.stream().limit(maxImages).toList();
    }

    private FlyerImageFetcher.FetchedImage fetchViaBrowser(String imageUrl) {
        boolean tabOpened = false;
        try {
            callTool("browser_tabs", Map.of("action", "new", "url", imageUrl));
            tabOpened = true;
            String text = callTool("browser_evaluate", Map.of("function", FETCH_SCRIPT));
            EvaluatedImage evaluated = JSON_HELPER.fromJson(jsonSection(text), EvaluatedImage.class);
            if (evaluated.data() == null || evaluated.data().isBlank()) {
                return null;
            }
            byte[] bytes = Base64.getDecoder().decode(evaluated.data());
            String mimeType = imageMimeType(bytes, evaluated.mime());
            return (mimeType == null) ? null : new FlyerImageFetcher.FetchedImage(bytes, mimeType);
        }
        catch (RuntimeException e) {
            context.log().accept("Browser fetch failed for " + imageUrl + ": " + e.getMessage() + "\n");
            return null;
        }
        finally {
            if (tabOpened) {
                try {
                    callTool("browser_tabs", Map.of("action", "close"));
                }
                catch (RuntimeException e) {
                    context.log().accept("Could not close the flyer tab: " + e.getMessage() + "\n");
                }
            }
        }
    }

    private String callTool(String name, Map<String, Object> arguments) {
        McpSchema.CallToolResult result = client.callTool(McpSchema.CallToolRequest.builder()
                .name(name)
                .arguments(arguments)
                .build());
        if (Boolean.TRUE.equals(result.isError())) {
            throw new IllegalStateException(name + " failed: " + contentText(result));
        }
        return contentText(result);
    }

    private static String contentText(McpSchema.CallToolResult result) {
        StringBuilder text = new StringBuilder();
        for (McpSchema.Content content : result.content()) {
            if (content instanceof McpSchema.TextContent textContent) {
                text.append(textContent.text());
            }
        }
        return text.toString();
    }

    // the MCP result wraps the evaluate output in a markdown section
    private static String jsonSection(String text) {
        int marker = text.lastIndexOf("### Result");
        String body = marker >= 0 ? text.substring(marker + "### Result".length()) : text;
        int start = body.indexOf('{');
        int end = body.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IllegalStateException("No JSON in MCP result: " + text);
        }
        return body.substring(start, end + 1);
    }

    private static URI resolve(String pageUrl, String src) {
        try {
            return URI.create(pageUrl).resolve(src);
        }
        catch (RuntimeException e) {
            return null;
        }
    }

    private static void addHost(Set<String> hosts, String url) {
        if (url == null) {
            return;
        }
        try {
            URI uri = URI.create(url);
            if (uri.getHost() != null
                    && ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) {
                hosts.add(GuardedMcpBrowserTool.normalizeHost(uri.getHost()));
            }
        }
        catch (RuntimeException e) {
            // ignore malformed resource URLs
        }
    }

    private static String transcription(String raw) {
        try {
            Document document = Jsoup.parse(raw, "", Parser.xmlParser());
            return document.getElementsByTag("transcription").text();
        }
        catch (RuntimeException e) {
            return "";
        }
    }

    private static String imageMimeType(byte[] bytes, String headerMimeType) {
        String detected = FlyerImageFetcher.detectImageMimeType(bytes);
        if (detected != null) {
            return detected;
        }
        if (headerMimeType == null) {
            return null;
        }
        String mimeType = headerMimeType.split(";")[0].trim().toLowerCase(Locale.ROOT);
        return mimeType.startsWith("image/") ? mimeType : null;
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private record PageImages(String pageUrl, String pageTitle, List<ImageInfo> images, List<String> observed) {
    }

    private record ImageInfo(String src, int w, int h, String alt) {
    }

    private record EvaluatedImage(String data, String mime) {
    }
}