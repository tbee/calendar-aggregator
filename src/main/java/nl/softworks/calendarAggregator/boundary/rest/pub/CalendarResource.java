package nl.softworks.calendarAggregator.boundary.rest.pub;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pub/calendar")
public class CalendarResource {

    // example http://localhost:8080/pub/calendar
    @GetMapping(value = "", produces = {"text/calendar"})
    public String greeting(HttpServletRequest request) {
        return "";
    }
}
