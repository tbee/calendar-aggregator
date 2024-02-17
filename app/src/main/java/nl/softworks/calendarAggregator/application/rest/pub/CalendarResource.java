package nl.softworks.calendarAggregator.application.rest.pub;

import jakarta.servlet.http.HttpServletRequest;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarLocation;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class CalendarResource {

    private static final Logger LOG = LoggerFactory.getLogger(CalendarResource.class);

    private static int EARTH_RADIUS = 6371;

    // example http://localhost:8080/ical
    @GetMapping(path = "/ical", produces = {"text/calendar"})
    public String ical(HttpServletRequest request, @RequestParam(defaultValue = "0.0") double lat, @RequestParam(defaultValue = "0.0") double lon, @RequestParam(defaultValue = "0") int d) {

        String timezones = R.timezone().findAll().stream()
                .map(tz -> tz.ical())
                .collect(Collectors.joining());

        String events = R.calendarEvent().findAll().stream()
                .filter(ce -> d == 0 || d > (int)calculateDistance(lat, lon, ce.calendarSource().calendarLocation().lat(), ce.calendarSource().calendarLocation().lon()))
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


    // example http://localhost:8080/
    @GetMapping(path = "/", produces = {"text/html"})
    public String html(HttpServletRequest request, @RequestParam(defaultValue = "0.0") double lat, @RequestParam(defaultValue = "0.0") double lon, @RequestParam(defaultValue = "0") int d) {

        String events = R.calendarEvent().findAll().stream()
                .filter(ce -> d == 0 || d > (int)calculateDistance(lat, lon, ce.calendarSource().calendarLocation().lat(), ce.calendarSource().calendarLocation().lon()))
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
                          <a href="%baseurl%/ical" target="_blank">%baseurl%/ical</a>
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
                          <a href="%baseurl%/?lat=51.9214012&lon=6.5761531&d=40" target="_blank">%baseurl%/?lat=51.9214012&lon=6.5761531&d=40</a>
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
                .flatMap(l -> icalWrap(75, l).stream())
                .collect(Collectors.joining("\r\n"));
    }

    private List<String> icalWrap(int cutOff, String s) {
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

        // https://www.kanzaki.com/docs/ical/location.html
        CalendarSource calendarSource = calendarEvent.calendarSource();
        CalendarLocation calendarLocation = calendarSource.calendarLocation();
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
				%exdate%
				END:VEVENT
				"""
                .replace("%uid%", calendarEvent.id() + "@dancemoments.softworks.nl")
                .replace("%tzid%", calendarLocation.timezone().name())
                .replace("%dtStart%", dateTimeFormatter.format(calendarEvent.startDateTime()))
                .replace("%dtEnd%", dateTimeFormatter.format(calendarEvent.endDateTime()))
                .replace("%summary%", (calendarLocation.name() + " " + calendarEvent.subject()).trim())
                .replace("%location%", calendarLocation.location().replace("\n", ", "))
                .replace("%description%", calendarLocation.url() + "\\n\\n" + settings.disclaimer())
                .replaceAll("(?m)^[ \t]*\r?\n", ""); // strip empty lines
    }

    private String tr(CalendarEvent calendarEvent) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("E yyyy-MM-dd HH:mm");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        CalendarSource calendarSource = calendarEvent.calendarSource();
        CalendarLocation calendarLocation = calendarSource.calendarLocation();

        String when = dateTimeFormatter.format(calendarEvent.startDateTime())
                + " - "
                + (calendarEvent.startDateTime().toLocalDate().equals(calendarEvent.endDateTime().toLocalDate()) ? timeFormatter : dateTimeFormatter).format(calendarEvent.endDateTime());

        String what = calendarLocation.name() + (calendarEvent.subject().isBlank() ? "" : " - " + calendarEvent.subject());

        return 	"""
				<tr>
				<td>%when%</td>
				<td>%what%</td>
				<td><a href="%url%" target="_blank">check here</a></td>
				</tr>
				"""
                .replace("%when%", when)
                .replace("%what%", what)
                .replace("%url%", calendarLocation.url())
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
