package nl.softworks.calendarAggregator.application.rest.pub;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pub")
public class CalendarResourceDeprecated {

    @GetMapping(path = "/calendar", produces = {"text/calendar"})
    public String calendar(HttpServletRequest request, @RequestParam(defaultValue = "0.0") double lat, @RequestParam(defaultValue = "0.0") double lon, @RequestParam(defaultValue = "0") int d) {
        return new CalendarResource().ical(request, lat, lon, d);
    }

    @GetMapping(path = "/ical", produces = {"text/calendar"})
    public String ical(HttpServletRequest request, @RequestParam(defaultValue = "0.0") double lat, @RequestParam(defaultValue = "0.0") double lon, @RequestParam(defaultValue = "0") int d) {
        return new CalendarResource().ical(request, lat, lon, d);
    }

    @GetMapping(path = "/html", produces = {"text/html"})
    public String html(HttpServletRequest request, @RequestParam(defaultValue = "0.0") double lat, @RequestParam(defaultValue = "0.0") double lon, @RequestParam(defaultValue = "0") int d) {
        return new CalendarResource().html(request, lat, lon, d);
    }
}
