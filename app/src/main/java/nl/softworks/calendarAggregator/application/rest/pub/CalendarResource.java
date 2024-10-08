package nl.softworks.calendarAggregator.application.rest.pub;

import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarLocation;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.Label;
import nl.softworks.calendarAggregator.domain.entity.Settings;
import nl.softworks.calendarAggregator.domain.entity.Timezone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class CalendarResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarResource.class);

    // example http://localhost:8080/ical
    @GetMapping(path = {"/ical", "/pub/calendar", "/pub/ical"}, produces = {"text/calendar"})
    public String ical(@RequestParam(defaultValue = "") Double lat, @RequestParam(defaultValue = "") Double lon, @RequestParam(defaultValue = "") Integer distance
            , @RequestParam(defaultValue = "false") Boolean showHidden
            , @RequestParam(defaultValue = "", name = "labelInclude") List<String> labelNamesInclude, @RequestParam(defaultValue = "", name = "labelExclude") List<String> labelNamesExclude) {

        List<Label> labelsInclude = CalendarController.labelsNameToEntities(labelNamesInclude);
        List<Label> labelsExclude = CalendarController.labelsNameToEntities(labelNamesExclude);
        String timezones = R.timezone().findAll().stream()
                .filter(tz -> tz.ical() != null && !tz.ical().isBlank())
                .map(Timezone::ical)
                .collect(Collectors.joining("\n"));

        // Collect events
        List<CalendarEvent> events = R.calendarEvent().findAll().stream()
                .filter(ce -> showHidden || !ce.calendarSource().hidden())
                .filter(ce -> CalendarController.filterEventOnDistance(ce, lat, lon, distance))
                .filter(ce -> CalendarController.filterEventOnLabels(ce, labelsInclude, labelsExclude))
                .toList();
        String eventsICAL = events.stream()
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
                        .replace("%events%", stripClosingNewline(eventsICAL))
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
                .replace("%tzid%", calendarSource.determineTimezone().name())
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
}
