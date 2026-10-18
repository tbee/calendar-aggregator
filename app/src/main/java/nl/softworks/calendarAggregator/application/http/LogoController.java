package nl.softworks.calendarAggregator.application.http;

import nl.softworks.calendarAggregator.domain.entity.Settings;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogoController {

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
}
