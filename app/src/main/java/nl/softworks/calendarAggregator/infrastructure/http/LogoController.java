package nl.softworks.calendarAggregator.infrastructure.http;

import nl.softworks.calendarAggregator.domain.entity.Settings;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.WebRequest;

import java.time.Duration;
import java.util.Arrays;

@Controller
public class LogoController {

    private static final MediaType ICON = MediaType.parseMediaType("image/x-icon");

    // the icon is built from the logo, which changes at most a few times a year, so the bytes are kept
    private volatile CachedIco cachedIco;

    /** @param etag the version of the logo the icon was made from */
    private record CachedIco(String etag, byte[] ico) {
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
        if (logo == null || logo.length == 0) {
            return ResponseEntity.notFound().build();
        }
        // the logo's content is the icon's version: a changed logo is a changed icon
        String etag = "\"" + logo.length + "-" + Integer.toHexString(Arrays.hashCode(logo)) + "\"";
        if (webRequest.checkNotModified(etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }
        byte[] ico = ico(etag, logo);
        if (ico == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok() //
                .contentType(ICON) //
                .eTag(etag) //
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePublic()) //
                .body(ico);
    }

    /** @return the icon for this version of the logo, scaled only once; null when the logo is not an image */
    private byte[] ico(String etag, byte[] logo) {
        CachedIco current = cachedIco;
        // two requests may convert at the same time after a change; the last one wins, which costs nothing
        if (current == null || !current.etag().equals(etag)) {
            current = new CachedIco(etag, Favicon.fromImage(logo));
            cachedIco = current;
        }
        return current.ico();
    }
}
