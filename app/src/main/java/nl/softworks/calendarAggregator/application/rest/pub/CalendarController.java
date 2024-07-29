package nl.softworks.calendarAggregator.application.rest.pub;

import jakarta.servlet.http.HttpServletRequest;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.Settings;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class CalendarController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarController.class);

    private static int EARTH_RADIUS = 6371;

    @RequestMapping(value = "/index")
    public String index(Model model, HttpServletRequest request, @RequestParam(defaultValue = "0.0") double lat, @RequestParam(defaultValue = "0.0") double lon, @RequestParam(defaultValue = "0") int d) {
        model.addAttribute("settings", Settings.get());
        model.addAttribute("lat", lat);
        model.addAttribute("lon", lon);
        model.addAttribute("d", d);

        // Collect events
        List<CalendarEvent> events = filterOnDistance(model, lat, lon, d);

        // List
        Map<LocalDateTime, List<CalendarEvent>> dateTimeToEventsMap = events.stream()
                .collect(Collectors.groupingBy(ce -> ce.startDateTime()));
        model.addAttribute("dateTimeToEventsMap", dateTimeToEventsMap);

        // When
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("E yyyy-MM-dd HH:mm");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        model.addAttribute("whenMap", events.stream()
                .map(event -> Pair.of(event,
                        dateTimeFormatter.format(event.startDateTime())
                        + " - "
                        + (event.startDateTime().toLocalDate().equals(event.endDateTime().toLocalDate()) ? timeFormatter : dateTimeFormatter).format(event.endDateTime())))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight)));

        // What
        model.addAttribute("whatMap", events.stream()
                .map(event -> Pair.of(event, event.calendarSource().calendarLocation().name() + (event.subject().isBlank() ? "" : " - " + event.subject())))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight)));

        return "list";
    }

    static List<CalendarEvent> filterOnDistance(Model model, double lat, double lon, int d) {
        model.addAttribute("lat", lat);
        model.addAttribute("lon", lon);
        model.addAttribute("d", d);

        // Collect events
        LocalDateTime threshold = LocalDateTime.now().minusHours(2);
        return R.calendarEvent().findAll().stream()
                .filter(ce -> ce.startDateTime().isAfter(threshold))
                .filter(ce -> d == 0 || d > (int) calculateDistance(lat, lon, ce.calendarSource().calendarLocation().lat(), ce.calendarSource().calendarLocation().lon()))
                .toList();
    }

    // https://www.baeldung.com/java-find-distance-between-points
    private static double calculateDistance(double startLat, double startLong, double endLat, double endLong) {

        double dLat = Math.toRadians(endLat - startLat);
        double dLong = Math.toRadians(endLong - startLong);

        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);

        double a = haversine(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversine(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
    private static double haversine(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}
