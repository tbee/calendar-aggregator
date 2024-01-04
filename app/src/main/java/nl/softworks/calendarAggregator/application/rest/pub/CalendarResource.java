package nl.softworks.calendarAggregator.application.rest.pub;

import jakarta.servlet.http.HttpServletRequest;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarEventExdate;
import nl.softworks.calendarAggregator.domain.entity.Settings;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pub")
public class CalendarResource {

    private static int EARTH_RADIUS = 6371;

    // example http://localhost:8080/pub/calendar
    @Deprecated
    @GetMapping(path = "/calendar", produces = {"text/calendar"})
    public String calendar(HttpServletRequest request) {
        return ical(request, 0.0, 0.0, 0);
    }

    // example http://localhost:8080/pub/ical
    @GetMapping(path = "/ical", produces = {"text/calendar"})
    public String ical(HttpServletRequest request, @RequestParam(defaultValue = "0.0") double lat, @RequestParam(defaultValue = "0.0") double lon, @RequestParam(defaultValue = "0") int d) {

        String timezones = R.timezone().findAll().stream()
                .map(tz -> tz.ical())
                .collect(Collectors.joining());

        LocalDateTime pastThreshold = LocalDateTime.now().minusMonths(1);
        LocalDateTime futureThreshold = LocalDateTime.now().plusMonths(4);
        String events = R.calendarEvent().findAll().stream()
                .filter(ce -> pastThreshold.isBefore(ce.startDateTime()) && futureThreshold.isAfter(ce.startDateTime()))
                .filter(ce -> d == 0 || d > (int)calculateDistance(lat, lon, ce.calendarSource().lat(), ce.calendarSource().lon()))
                .map(this::ical)
                .collect(Collectors.joining());

        Settings settings = Settings.get();

        return icalFormat(
                """
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//Softworks//NONSGML %title%//EN
                CALSCALE:GREGORIAN
                METHOD:PUBLISH
                REFRESH-INTERVAL;VALUE=DURATION:P1D
                X-PUBLISHED-TTL:P1D
                %timezones%
                %events%
                END:VCALENDAR
                """
                .replace("%title%", settings.title())
                .replace("%timezones%", stripClosingNewline(timezones))
                .replace("%events%", stripClosingNewline(events))
        );
    }


    // example http://localhost:8080/pub/html
    @GetMapping(path = "/html", produces = {"text/html"})
    public String html(HttpServletRequest request, @RequestParam(defaultValue = "0.0") double lat, @RequestParam(defaultValue = "0.0") double lon, @RequestParam(defaultValue = "0") int d) {

        LocalDateTime pastThreshold = LocalDateTime.now().minusHours(1);
        LocalDateTime futureThreshold = LocalDateTime.now().plusMonths(5);
        String events = R.calendarEvent().findAll().stream()
                .flatMap(ce -> ce.applyRRule().stream()) // for HTML we need to generate the actual events
                .filter(ce -> pastThreshold.isBefore(ce.startDateTime()) && futureThreshold.isAfter(ce.startDateTime()))
                .filter(ce -> d == 0 || d > (int)calculateDistance(lat, lon, ce.calendarSource().lat(), ce.calendarSource().lon()))
                .sorted(Comparator.comparing(CalendarEvent::startDateTime))
                .map(this::tr)
                .collect(Collectors.joining());

        Settings settings = Settings.get();

        return  """
                <html>
                  <head>
                    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bulma@0.9.4/css/bulma.min.css">
                  </head>
                  <body>
                    <section class="section">
                      <h1 class="title">%title%</h1>
                      <h2 class="subtitle">%subtitle%</h2>
                      <div class="block" style="max-width:1000px">%disclaimer%</div>
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
                      <div class="notification" style="max-width:1000px">
                        <p>
                          This list is also available in calendar form.
                          You can add it to, for example, Google calendar by adding an external URL calendar using the following URL:
                        </p>
                        <p style="margin-top:5px;">
                          <a href="%baseurl%/pub/ical" target="_blank">%baseurl%/pub/ical</a>
                        </p>
                      </div>
                      <div class="notification" style="max-width:1000px">
                        <p>
                          You can limit the amount of entries by filtering on distance (as the crow flies).
                          For this you need to determine the decimal latitude (lat) and longitude (lon) of where you live, for example by using Google maps.
                          Then add these as parameters to the URL, together with a distance (d) in kilometers. 
                          For example:
                        </p>
                        <p style="margin-top:5px;">
                          <a href="%baseurl%/pub/html?lat=51.9214012&lon=6.5761531&d=40" target="_blank">%baseurl%/pub/html?lat=51.9214012&lon=6.5761531&d=40</a>
                        </p>
                        <p style="margin-top:5px;">
                          The same parameters can be set on the URL for the calendar.
                        </p>
                      </div>
                    </section>
                  </body>
                </html>
                """.replace("%title%", settings.title())
                   .replace("%subtitle%", settings.subtitle())
                   .replace("%baseurl%", settings.websiteBaseurl())
                   .replace("%disclaimer%", settings.disclaimer())
                   .replace("%events%", stripClosingNewline(events));
    }

    private String stripClosingNewline(String s) {
        while (s.endsWith("\n")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private String icalFormat(String s) {
        return s.replace("\r", "")
                .lines()
                .flatMap(l -> wrap(75, l).stream())
                .collect(Collectors.joining("\r\n"));
    }

    private List<String> wrap(int cutOff, String s) {
        cutOff--; // compensate for the space that is prefixed
        List<String> lines = new ArrayList<>();
        while (s.length() > cutOff) {
            lines.add((lines.isEmpty() ? "" : " ") + s.substring(0, cutOff));
            s = s.substring(cutOff);
        }
        if (!s.isEmpty()) {
            lines.add((lines.isEmpty() ? "" : " ") + s);
        }
        return lines;
    }

    private String ical(CalendarEvent calendarEvent) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        Settings settings = Settings.get();

        // Create EXDATE value
        // EXDATE:19960402T010000Z,19960403T010000Z,19960404T010000Z
        String exdate = calendarEvent.calendarEventExdates().stream()
                .map(CalendarEventExdate::excludedDate)
                .map(ld -> LocalDateTime.of(ld, calendarEvent.startDateTime().toLocalTime()))
                .map(ldt -> dateTimeFormatter.format(ldt) + "Z")
                .collect(Collectors.joining(","));

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
				%rrule%
				%exdate%
				END:VEVENT
				"""
                .replace("%uid%", calendarEvent.id() + "@dancemoments.softworks.nl")
                .replace("%tzid%", calendarEvent.calendarSource().timezone().name())
                .replace("%dtStart%", dateTimeFormatter.format(calendarEvent.startDateTime()))
                .replace("%dtEnd%", dateTimeFormatter.format(calendarEvent.endDateTime()))
                .replace("%summary%", (calendarEvent.calendarSource().name() + " " + calendarEvent.subject()).trim())
                .replace("%location%", calendarEvent.calendarSource().location().replace("\n", ", "))
                .replace("%description%", calendarEvent.calendarSource().url() + "\\n\\n" + settings.disclaimer())
                .replace("%rrule%", (calendarEvent.rrule().isBlank() ? "" : "RRULE:" + calendarEvent.rrule()))
                .replace("%exdate%", (exdate.isBlank() ? "" : "EXDATE:" + exdate))
                .replaceAll("(?m)^[ \t]*\r?\n", ""); // strip empty lines
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
				<td><a href="%url%" target="_blank">check here</a></td>
				</tr>
				"""
                .replace("%when%", when)
                .replace("%what%", what)
                .replace("%url%", calendarEvent.calendarSource().url())
                ;
    }

    // https://www.baeldung.com/java-find-distance-between-points
    private double calculateDistance(double startLat, double startLong, double endLat, double endLong) {

        double dLat = Math.toRadians(endLat - startLat);
        double dLong = Math.toRadians(endLong - startLong);

        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);

        double a = haversine(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversine(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
    private double haversine(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}