package nl.softworks.calendarAggregator.infrastructure.http;

import nl.softworks.calendarAggregator.domain.entity.Settings;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;

@Controller
public class LogoController {

    private static final MediaType ICON = MediaType.parseMediaType("image/x-icon");

    /** What the site shows as long as no logo is set, or when the logo cannot be read as an image. */
    private static final Icon DEFAULT_ICON = defaultIcon();

    // the icon is built from the logo, which changes at most a few times a year, so the bytes are kept
    private volatile Icon cachedIcon;

    /** @param etag the version of the logo the icon was made from */
    private record Icon(String etag, byte[] bytes) {
    }

    @GetMapping("/logo")
    public ResponseEntity<byte[]> logo() {
        Settings settings = Settings.get();
        byte[] logo = settings.logo();
        if (logo == null || logo.length == 0) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = settings.logoMimetype() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(settings.logoMimetype());
        return ResponseEntity.ok().contentType(mediaType).body(logo);
    }

    // the icon is derived from the logo, so browsers get one without a separate upload
    @GetMapping("/favicon.ico")
    public ResponseEntity<byte[]> favicon(WebRequest webRequest) {
        byte[] logo = Settings.get().logo();
        Icon icon = logo == null || logo.length == 0 ? DEFAULT_ICON : iconOf(logo);
        if (webRequest.checkNotModified(icon.etag())) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }
        return ResponseEntity.ok() //
                .contentType(ICON) //
                .eTag(icon.etag()) //
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePublic()) //
                .body(icon.bytes());
    }

    /** @return the icon for this logo, scaled only once; the bundled icon when the logo is not an image */
    private Icon iconOf(byte[] logo) {
        // the logo's content is the icon's version: a changed logo is a changed icon
        String etag = "\"" + logo.length + "-" + Integer.toHexString(Arrays.hashCode(logo)) + "\"";
        Icon current = cachedIcon;
        // two requests may convert at the same time after a change; the last one wins, which costs nothing
        if (current == null || !current.etag().equals(etag)) {
            byte[] ico = Favicon.fromImage(logo);
            current = ico == null ? DEFAULT_ICON : new Icon(etag, ico);
            cachedIcon = current;
        }
        return current;
    }

    /** The icon that ships with the application, served as long as no logo of its own can be made. */
    private static Icon defaultIcon() {
        try (InputStream inputStream = new ClassPathResource("static/favicon.ico").getInputStream()) {
            return new Icon("\"default\"", inputStream.readAllBytes());
        } catch (IOException e) {
            // the file is part of the jar, so this only happens in a broken build
            throw new IllegalStateException("the bundled favicon.ico cannot be read", e);
        }
    }
}
