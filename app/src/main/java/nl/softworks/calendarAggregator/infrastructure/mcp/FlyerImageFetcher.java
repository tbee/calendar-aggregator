package nl.softworks.calendarAggregator.infrastructure.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Downloads a flyer image over plain HTTP, with the browser's headers. This is the cheap
 * path; the browser replay in {@link FlyerInspector} is the fallback for hotlink or
 * signature protected images.
 */
@Service
public class FlyerImageFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlyerImageFetcher.class);
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";

    @Value("${calendaraggregator.aiAgent.flyerMaxBytes:20971520}")
    private int maxBytes;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    public record FetchedImage(byte[] bytes, String mimeType) {
    }

    public Optional<FetchedImage> fetch(String imageUrl, String pageUrl, Set<String> allowedHosts) {
        try {
            URI uri = URI.create(imageUrl);
            for (int redirects = 0; redirects <= 3; redirects++) {
                if (!isAllowed(uri, allowedHosts)) {
                    LOGGER.debug("Flyer image refused: {}", uri);
                    return Optional.empty();
                }

                HttpRequest.Builder request = HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofSeconds(20))
                        .header("User-Agent", USER_AGENT)
                        .header("Accept", "image/avif,image/webp,image/png,image/*,*/*;q=0.8")
                        .GET();
                if (pageUrl != null && !pageUrl.isBlank()) {
                    request.header("Referer", pageUrl);
                }

                HttpResponse<InputStream> response = httpClient.send(request.build(), HttpResponse.BodyHandlers.ofInputStream());
                try (InputStream body = response.body()) {
                    int status = response.statusCode();
                    if (isRedirect(status)) {
                        String location = response.headers().firstValue("location").orElse(null);
                        if (location == null) {
                            return Optional.empty();
                        }
                        uri = uri.resolve(location);
                        continue;
                    }
                    if (status != 200) {
                        LOGGER.debug("Flyer image {} returned HTTP {}", uri, status);
                        return Optional.empty();
                    }
                    long contentLength = response.headers().firstValueAsLong("content-length").orElse(-1);
                    if (contentLength > maxBytes) {
                        LOGGER.debug("Flyer image {} too large: {} bytes", uri, contentLength);
                        return Optional.empty();
                    }
                    byte[] bytes = readBounded(body, maxBytes);
                    if (bytes == null) {
                        LOGGER.debug("Flyer image {} too large", uri);
                        return Optional.empty();
                    }
                    String mimeType = detectImageMimeType(bytes);
                    if (mimeType == null) {
                        LOGGER.debug("Flyer image {} is not a supported image", uri);
                        return Optional.empty();
                    }
                    return Optional.of(new FetchedImage(bytes, mimeType));
                }
            }
            return Optional.empty();
        }
        catch (IOException | InterruptedException | RuntimeException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.debug("Could not download flyer image " + imageUrl, e);
            return Optional.empty();
        }
    }

    public boolean isAllowed(String imageUrl, Set<String> allowedHosts) {
        try {
            return isAllowed(URI.create(imageUrl), allowedHosts);
        }
        catch (RuntimeException e) {
            return false;
        }
    }

    // only http(s) on hosts observed on the page, and never on a private address
    private boolean isAllowed(URI uri, Set<String> allowedHosts) {
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https")) {
            return false;
        }
        String host = uri.getHost();
        if (host == null || host.isBlank() || !allowedHosts.contains(GuardedMcpBrowserTool.normalizeHost(host))) {
            return false;
        }
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (isPrivate(address)) {
                    return false;
                }
            }
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    private static boolean isPrivate(InetAddress address) {
        return address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress();
    }

    private static boolean isRedirect(int status) {
        return status == 301 || status == 302 || status == 303 || status == 307 || status == 308;
    }

    private byte[] readBounded(InputStream inputStream, int max) throws IOException {
        byte[] buffer = new byte[8192];
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            if (output.size() + read > max) {
                return null;
            }
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    static String detectImageMimeType(byte[] b) {
        if (b.length >= 4 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "image/png";
        }
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (b.length >= 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F') {
            return "image/gif";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        return null;
    }
}