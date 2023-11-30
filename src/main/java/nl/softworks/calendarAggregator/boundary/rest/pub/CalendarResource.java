package nl.softworks.calendarAggregator.boundary.rest.pub;

import jakarta.servlet.http.HttpServletRequest;
import nl.softworks.calendarAggregator.domain.boundary.R;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pub/calendar")
public class CalendarResource {


    // example http://localhost:8080/pub/calendar
    @GetMapping(value = "", produces = {"text/calendar"})
    public String calendar(HttpServletRequest request) {

        String timezones = R.timezoneRepo().findAll().stream()
                .map(tz -> tz.ical())
                .collect(Collectors.joining());

        LocalDateTime pastThreshold = LocalDateTime.now().minusMonths(1);
        LocalDateTime futureThreshold = LocalDateTime.now().plusMonths(4);
        String events = R.calendarEvent().findAll().stream()
                .filter(e -> pastThreshold.isBefore(e.startDateTime()) && futureThreshold.isAfter(e.startDateTime()))
                .map(ce -> ce.ical())
                .collect(Collectors.joining());
        
        return crlf(
                """
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//Calendar Aggregator//NONSGML Calendar Aggregator//EN
                CALSCALE:GREGORIAN
                METHOD:PUBLISH
                REFRESH-INTERVAL;VALUE=DURATION:PT8H
                X-Robots-Tag:noindex
                X-PUBLISHED-TTL:PT1H
                %timezones%
                %events%
                END:VCALENDAR
                """
                .replace("%timezones%", stripClosingNewline(timezones))
                .replace("%events%", stripClosingNewline(events))
        );
    }

    private String stripClosingNewline(String s) {
        while (s.endsWith("\n")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private String crlf(String s) {
        return s.replace("\r", "")
                .replace("\n", "\r\n");

    }
}
