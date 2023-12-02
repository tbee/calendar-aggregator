package nl.softworks.calendarAggregator.boundary.rest.pub;

import jakarta.servlet.http.HttpServletRequest;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pub")
public class CalendarResource {

    // example http://localhost:8080/pub/calendar
    @GetMapping(path = "/calendar", produces = {"text/calendar"})
    public String calendar(HttpServletRequest request) {
        return ical(request);
    }

    // example http://localhost:8080/pub/ical
    @GetMapping(path = "/ical", produces = {"text/calendar"})
    public String ical(HttpServletRequest request) {

        String timezones = R.timezoneRepo().findAll().stream()
                .map(tz -> tz.ical())
                .collect(Collectors.joining());

        LocalDateTime pastThreshold = LocalDateTime.now().minusMonths(1);
        LocalDateTime futureThreshold = LocalDateTime.now().plusMonths(4);
        String events = R.calendarEvent().findAll().stream()
                .filter(e -> pastThreshold.isBefore(e.startDateTime()) && futureThreshold.isAfter(e.startDateTime()))
                .map(this::ical)
                .collect(Collectors.joining());

        return crlf(
                """
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//Softworks//NONSGML Dance moments//EN
                CALSCALE:GREGORIAN
                METHOD:PUBLISH
                REFRESH-INTERVAL;VALUE=DURATION:P1D
                X-PUBLISHED-TTL:P1D
                %timezones%
                %events%
                END:VCALENDAR
                """
                .replace("%timezones%", stripClosingNewline(timezones))
                .replace("%events%", stripClosingNewline(events))
        );
    }


    // example http://localhost:8080/pub/html
    @GetMapping(path = "/html", produces = {"text/html"})
    public String html(HttpServletRequest request) {

        String timezones = R.timezoneRepo().findAll().stream()
                .map(tz -> tz.ical())
                .collect(Collectors.joining());

        LocalDateTime pastThreshold = LocalDateTime.now().minusDays(1);
        LocalDateTime futureThreshold = LocalDateTime.now().plusMonths(5);
        String events = R.calendarEvent().findAll().stream()
                .filter(e -> pastThreshold.isBefore(e.startDateTime()) && futureThreshold.isAfter(e.startDateTime()))
                .sorted(Comparator.comparing(CalendarEvent::startDateTime))
                .map(this::tr)
                .collect(Collectors.joining());

        return crlf(
                """
                <html>
                  <head>
                    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bulma@0.9.4/css/bulma.min.css">
                  </head>
                  <body>
                    <section class="section">
                      <h1 class="title">Dance moments</h1>
                      <h2 class="subtitle">Ballroom en latin</h2>
                      <table class="table">
                        <thead>
				          <tr>
				            <td>When</td>
				            <td>What</td>
				            <td>Web</td>
				          </tr>
                        </thead>
                        <tbody>
                          %events%
                        </tbody>
                      </table>
                    </section>
                  </body>
                </html>
                """.replace("%events%", stripClosingNewline(events))
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

    private String ical(CalendarEvent calendarEvent) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

        // https://www.kanzaki.com/docs/ical/location.html
        return 	"""
				BEGIN:VEVENT
				UID:%uid%
				DTSTAMP:%dtStart%
				DTSTART;TZID=%tzid%:%dtStart%
				DTEND;TZID=%tzid%:%dtEnd%
				TRANSP:OPAQUE
				CLASS:PUBLIC
				SUMMARY:%summary%
				DESCRIPTION:%description%
				LOCATION:%location%
				END:VEVENT
				"""
                .replace("%uid%", calendarEvent.id() + "@dancemoments.softworks.nl")
                .replace("%tzid%", calendarEvent.calendarSource().timezone().name())
                .replace("%dtStart%", dateTimeFormatter.format(calendarEvent.startDateTime()))
                .replace("%dtEnd%", dateTimeFormatter.format(calendarEvent.endDateTime()))
                .replace("%summary%", (calendarEvent.calendarSource().name() + " " + calendarEvent.subject()).trim())
                .replace("%location%", calendarEvent.calendarSource().location().replace("\n", ", "))
                .replace("%description%", calendarEvent.calendarSource().url())
                ;
    }

    private String tr(CalendarEvent calendarEvent) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("E yyyy-MM-dd HH:mm");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String when = dateTimeFormatter.format(calendarEvent.startDateTime())
                + " - "
                + (calendarEvent.startDateTime().toLocalDate().equals(calendarEvent.endDateTime().toLocalDate()) ? timeFormatter : dateTimeFormatter).format(calendarEvent.endDateTime());

        String what = calendarEvent.calendarSource().name() + (calendarEvent.subject().isBlank() ? "" : " - " + calendarEvent.subject());

        return 	"""
				<tr>
				<td>%when%</td>
				<td>%what%</td>
				<td><a href="%url%" target="_blank">info</a></td>
				</tr>
				"""
                .replace("%when%", when)
                .replace("%what%", what)
                .replace("%url%", calendarEvent.calendarSource().url())
                ;
    }
}
