package nl.softworks.calendarAggregator.application.rest.pub;

import jakarta.servlet.http.HttpServletRequest;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarLocation;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceLabelAssignment;
import nl.softworks.calendarAggregator.domain.entity.Label;
import nl.softworks.calendarAggregator.domain.entity.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class CalendarResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarResource.class);

    private static int EARTH_RADIUS = 6371;

    private String pagetemplate() {
        Settings settings = Settings.get();
        return """
                <html>
                  <head>
                    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bulma@1.0.0/css/bulma.min.css">
                    <script src="https://kit.fontawesome.com/501b8808a2.js" crossorigin="anonymous"></script>
                    <style>
                      .centered {
                        text-align: center;
                      }
                      .alignright {
                        text-align: right;
                      }
                      .bullet {
                        list-style: square outside;
                        margin-left: 20px;
                        padding-left: 0;
                      }
                        .css-icon {
                          font-family: "Work Sans", sans-serif;
                          font-weight: 800;
                          font-style: normal;
                          font-size: 14px;
                          text-decoration: none;
                          display: block;
                          margin: 0 auto;
                          height: 14px;
                          width: 14px;
                          border-radius: 2px;
                          overflow: hidden;
                          border: solid 1px transparent;
                        }
                        .css-icon span {
                          margin-top:-2px;
                          font-size: 80%;
                          text-align: center;
                          display: block;
                        }
                        .css-icon-eventtype {
                          background: #6da795;
                          color: #ffffff;
                        }
                        .css-icon-dancestyle {
                          background: #ad7765;
                          color: #ffffff;
                        }
                    </style>
                    <!--pageheader-->
                  </head>
                  <body>
                    <section class="section">
                      <h1 class="title">%title%</h1>
                      <h2 class="subtitle">%subtitle%</h2>
                      <div class="block pagewidth">%disclaimer%</div>
                      <div id="iconbar" class="block alignright pagewidth">
                        <span class="icon">
                          <a href="%baseurl%/list"><i class="fas fa-list fa-xl"></i></a>
                        </span>
                        <span class="icon">
                          <a href="%baseurl%/"><i class="fas fa-calendar-days fa-xl"></i></a>
                        </span>
                      </div>
                      <!--pagecontent-->
                      <div class="notification pagewidth" style="margin-top:10px;">
                        <p>
                          This data is available in:
                        </p>
                        <ul>
                          <li class="bullet"><a href="%baseurl%/list">List</a> form</li>
                          <li class="bullet"><a href="%baseurl%/">Calendar</a> form</li>
                          <li class="bullet">For including in e.g. Google calendar by adding an external URL calendar using the following URL: <a href="%baseurl%/ical" target="_blank">%baseurl%/ical</a></li>
                        </ul>
                      </div>
                      <div class="notification pagewidth">
                        <form method="get">
                            <div class="field">
                                <p>
                                  You can limit the amount of entries by filtering on distance (as the crow flies).
                                  For this you need to determine the decimal latitude and longitude of where you live, for example by using Google maps.
                                  These parameters can also be applied to the ical url.
                                </p>
                            </div>
                            <div class="field is-horizontal">
                              <div class="field-label is-normal">
                                <label class="label">Latitude</label>
                              </div>
                              <div class="field-body">
                                <div class="field">
                                  <p class="control">
                                    <input name="lat" class="input" type="number" step="0.0000001" min="-90.0" max="90.0" placeholder="e.g. 51.9214012"/>
                                  </p>
                                </div>
                              </div>
                            </div>
                            <div class="field is-horizontal">
                              <div class="field-label is-normal">
                                <label class="label">Longitude</label>
                              </div>
                              <div class="field-body">
                                <div class="field">
                                  <p class="control">
                                    <input name="lon" class="input" type="number" step="0.0000001" min="-180.0" max="180.0" placeholder="e.g 6.5761531"/>
                                  </p>
                                </div>
                              </div>
                            </div>
                            <div class="field is-horizontal">
                              <div class="field-label is-normal">
                                <label class="label">Distance</label>
                              </div>
                              <div class="field-body">
                                <div class="field">
                                  <p class="control">
                                    <input name="d" class="input" type="number" step="1" min="0" max="500" placeholder="e.g. 50"/>
                                  </p>
                                </div>
                              </div>
                            </div>
                            <div class="field is-horizontal">
                              <div class="field-label is-normal">
                              </div>
                              <div class="field-body">
                                <div class="field">
                                  <p class="control">
                                    <input class="button" type="submit" value="Go!" />
                                  </p>
                                </div>
                              </div>
                            </div>
                        </form>
                      </div>
                    </section>
                  </body>
                </html>
                """
                .replace("%title%", settings.title())
                .replace("%subtitle%", settings.subtitle())
                .replace("%baseurl%", settings.websiteBaseurl())
                .replace("%disclaimer%", settings.disclaimer());
    }

    // example http://localhost:8080/list
    @GetMapping(path = {"/list", "/pub/html"}, produces = {"text/html"})
    public String htmllist(HttpServletRequest request, @RequestParam(defaultValue = "0.0") double lat, @RequestParam(defaultValue = "0.0") double lon, @RequestParam(defaultValue = "0") int d) {

        // Collect events
        LocalDateTime threshold = LocalDateTime.now().minusHours(2);
        AtomicReference<LocalDate> lastStartDateTimeRef = new AtomicReference<>();
        String events = R.calendarEvent().findAll().stream()
                .filter(ce -> ce.startDateTime().isAfter(threshold))
                .filter(ce -> d == 0 || d > (int) calculateDistance(lat, lon, ce.calendarSource().calendarLocation().lat(), ce.calendarSource().calendarLocation().lon()))
                .sorted(Comparator.comparing(CalendarEvent::startDateTime))
                .map(ce -> {
                    LocalDate startDateTime = ce.startDateTime().toLocalDate();
                    boolean dateChange = (lastStartDateTimeRef.get() != null && !lastStartDateTimeRef.get().equals(startDateTime));
                    lastStartDateTimeRef.set(startDateTime); // This is a side effect, but we need some way to detect a date change and stream gathers are still a way off
                    return tr(ce, dateChange);
                })
                .collect(Collectors.joining());

        return pagetemplate()
                .replace("<!--pageheader-->", """
                  <style>
                    .pagewidth {
                      max-width:1000px;
                    }
                  </style>
                """)
                .replace("<!--pagecontent-->", """
                      <table class="table is-hoverable">
                        <thead>
                          <tr>
                            <td>When</td>
                            <td>What</td>
                            <td>Check here</td>
                          </tr>
                        </thead>
                        <tbody>
                          %events%
                        </tbody>
                      </table>
                """)
                .replace("%events%", events.toString());
    }


    private String tr(CalendarEvent calendarEvent, boolean dateChange) {
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
				<td style='%dateChange%'>%when%</td>
				<td style='%dateChange%'>%what%</td>
				<td style='%dateChange%'>
                 <span class="icon">
                   <a href="%url%" target="_blank"><i class="fas fa-arrow-up-right-from-square"></i></a>
                 </span>
				</td>
				</tr>
				"""
                .replace("%dateChange%", dateChange ? "border-top-width:2px;" : "")
                .replace("%when%", when)
                .replace("%what%", what)
                .replace("%url%", calendarLocation.url())
                ;
    }

    // example http://localhost:8080/month
    @GetMapping(path = {"/month", "/htmlmonth", "/"}, produces = {"text/html"})
    public String htmlmonth(HttpServletRequest request, @RequestParam(defaultValue = "0.0") double lat, @RequestParam(defaultValue = "0.0") double lon, @RequestParam(defaultValue = "0") int d
            , @RequestParam(defaultValue = "0") int year, @RequestParam(defaultValue = "0") int month) {

        // Default parameter values
        if (year == 0 || month == 0) {
            LocalDate now = LocalDate.now();
            year = now.getYear();
            month = now.getMonthValue();
        }
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

        // start at monday
        LocalDate renderStart = monthStart.minusDays(monthStart.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
        LocalDate renderEnd = monthEnd.plusDays(DayOfWeek.SUNDAY.getValue() - monthEnd.getDayOfWeek().getValue());

        // Collect events
        Map<LocalDate, List<CalendarEvent>> dateToCalendarEvents = R.calendarEvent().findAll().stream()
                .filter(ce -> !ce.startDateTime().toLocalDate().isAfter(renderEnd))
                .filter(ce -> !ce.endDateTime().toLocalDate().isBefore(renderStart))
                .filter(ce -> d == 0 || d > (int) calculateDistance(lat, lon, ce.calendarSource().calendarLocation().lat(), ce.calendarSource().calendarLocation().lon()))
                .sorted(Comparator.comparing(CalendarEvent::startDateTime))
                .collect(Collectors.groupingBy(ce -> ce.startDateTime().toLocalDate()));

        // Render day table cells
        StringBuilder tbody = new StringBuilder();
        LocalDate render = renderStart;
        DateTimeFormatter yyyy = DateTimeFormatter.ofPattern("yyyy", Locale.ENGLISH);
        DateTimeFormatter mmm = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
        DateTimeFormatter dd = DateTimeFormatter.ofPattern("d", Locale.ENGLISH);
        DateTimeFormatter hhmm = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
        tbody.append("<tr>");
        while (!render.isAfter(renderEnd)) {
            if (render.getDayOfWeek() == DayOfWeek.MONDAY) {
                tbody.append("</tr><tr>");
            }

            boolean outsideMonth = render.isBefore(monthStart) || render.isAfter(monthEnd);

            List<CalendarEvent> calendarEvents = dateToCalendarEvents.get(render);
            calendarEvents = (calendarEvents == null ? List.of() : calendarEvents);
            String events = "<ul>" +
                    calendarEvents.stream()
                    .map(ce -> {
                        String description = ce.determineSubject();
                        List<Label> labels = ce.calendarSource().labelAssignments().stream()
                                .filter(la -> la.subjectRegexp().isBlank() || description.matches(la.subjectRegexp()))
                                .map(CalendarSourceLabelAssignment::label)
                                .toList();
                        String labelHTML = labels.stream()
                                .sorted(Comparator.comparing(Label::seqnr))
                                .map(l -> """
                                        <div class="tooltipped">
                                        <i class="css-icon css-icon-%labelgroup%" style="%color%"><span>%icon%</span></i>
                                        <span class="tooltiptext">%tooltip%</span>
                                        </div>
                                        """
                                        .replace("%icon%", l.icon())
                                        .replace("%labelgroup%", l.labelGroup().name().replace(" ", "").toLowerCase())
                                        .replace("%color%", "color:" + l.labelGroup().color() + "; background:" + l.labelGroup().background() + ";")
                                        .replace("%tooltip%", l.description()))
                                .collect(Collectors.joining("\n"));
                        return """
                                     <li>
                                       <div class="tooltipped">
                                         <span>%text%</span>
                                         <span class="tooltiptext">%description%</span>
                                       </div>
                                       <span class="icon" style="width:%labelswidth%px; height:10px;">
                                         %labels%
                                       </span>
                                       <span class="icon" style="width:12px; height:10px;">
                                         <a href="%url%" target="_blank"><i class="fas fa-arrow-up-right-from-square fa-xs"></i></a>
                                       </span>
                                     </li>
                                   """
                                   .replace("%text%", hhmm.format(ce.startDateTime()) + " " + ce.calendarSource().calendarLocation().name())
                                   .replace("%labelswidth%", "" + (labels.size() * 12))
                                   .replace("%labels%", labelHTML)
                                   .replace("%description%", (description.isBlank() ? "See the website" : description))
                                   .replace("%url%", ce.calendarSource().determineUrl());
                    })
                    .collect(Collectors.joining(""))
                    + "</ul>";

            LocalDate today = LocalDate.now();
            tbody.append("""
                    <td class="%inOrOutsideMonth% %today%">
                        <h5 class="title is-5">%date%</h5>
                        <div class="">
                            %events%
                        </div>
                    </td>
                    """
                    .replace("%inOrOutsideMonth%", outsideMonth ? "outside-month" : "inside-month")
                    .replace("%today%", today.equals(render) ? "today" : "")
                    .replace("%date%", dd.format(render))
                    .replace("%events%", events));
            render = render.plusDays(1);
        }
        tbody.append("</tr>");

        LocalDate prevMonth = monthStart.minusMonths(1);
        LocalDate nextMonth = monthStart.plusMonths(1);
        Settings settings = Settings.get();
        return pagetemplate()
                .replace("<!--pageheader-->", """
                  <style>
                    .today {
                      background: #c0c0c0!important;
                    }
                    .pagewidth {
                      width:100%;
                    }
                    
                    #calendarwrapper {
                      overflow-x: auto;
                    }
                    #calendar {
                      width:100%;
                    }
                    tbody td {
                      height:5em;
                      white-space: nowrap;
                      font-size: 13px;
                    }
                    tbody h5 {
                      margin-bottom: 0!important;
                    }
                    .outside-month > h5 {
                      color: lightgray!important;
                    }
                    .border {
                      border: 2px solid;
                    }
                    
                    .tooltipped {
                      position: relative;
                      display: inline-block;
                      xxxborder-bottom: 1px dotted black;
                    }
                    .tooltipped .tooltiptext {
                      visibility: hidden;
                      background-color: gray;
                      color: white;
                      text-align: center;
                      border-radius: 6px;
                      padding: 5px;
                    
                      /* Position the tooltip */
                      position: absolute;
                      z-index: 1;
                    }
                    .tooltipped:hover .tooltiptext {
                      visibility: visible;
                    }
                  </style>
                """)
                .replace("<!--pagecontent-->", """
                      <div class="columns is-gapless">
                        <div class="column alignright">
                          <span class="icon">
                            <a href="%baseurl%/?year=%prevyear%&month=%prevmonth%"><i class="fas fa-arrow-left fa-2xl"></i></a>
                          </span>
                        </div>
                        <div class="column is-one-fifth">
                          <h3 class="title is-4 centered">%year%</h3>
                          <h3 class="subtitle is-4 centered"> %month%</h3>
                        </div>
                        <div class="column">
                          <span class="icon">
                            <a href="%baseurl%/?year=%nextyear%&month=%nextmonth%"><i class="fas fa-arrow-right fa-2xl"></i></a>
                          </span>
                        </div>
                      </div>
                      <div id="calendarwrapper">
                        <table id="calendar" class="table">
                          <thead>
                            <tr>
                              <td width="12%">Monday</td>
                              <td width="12%">Tuesday</td>
                              <td width="12%">Wednesday</td>
                              <td width="12%">Thursday</td>
                              <td width="12%">Friday</td>
                              <td width="12%">Saturday</td>
                              <td width="12%">Sunday</td>
                            </tr>
                          </thead>
                          <tbody>
                              %tbody%
                          </tbody>
                        </table>
                      </div>
                """)
                .replace("%year%", yyyy.format(monthStart))
                .replace("%month%", mmm.format(monthStart))
                .replace("%prevyear%", "" + prevMonth.getYear())
                .replace("%prevmonth%", "" + prevMonth.getMonthValue())
                .replace("%nextyear%", "" + nextMonth.getYear())
                .replace("%nextmonth%", "" + nextMonth.getMonthValue())
                .replace("%baseurl%", settings.websiteBaseurl())
                .replace("%tbody%", tbody.toString());
    }

    // example http://localhost:8080/ical
    @GetMapping(path = {"/ical", "/pub/calendar", "/pub/ical"}, produces = {"text/calendar"})
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
				END:VEVENT
				"""
                .replace("%uid%", calendarEvent.id() + "@dancemoments.softworks.nl")
                .replace("%tzid%", calendarLocation.timezone().name())
                .replace("%dtStart%", dateTimeFormatter.format(calendarEvent.startDateTime()))
                .replace("%dtEnd%", dateTimeFormatter.format(calendarEvent.endDateTime()))
                .replace("%summary%", (calendarLocation.name() + " " + calendarEvent.subject()).trim())
                .replace("%location%", calendarLocation.location().replace("\n", ", "))
                .replace("%description%", calendarSource.determineUrl() + "\\n\\n" + settings.disclaimer())
                .replaceAll("(?m)^[ \t]*\r?\n", ""); // strip empty lines
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
