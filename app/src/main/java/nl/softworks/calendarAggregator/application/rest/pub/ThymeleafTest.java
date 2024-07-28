package nl.softworks.calendarAggregator.application.rest.pub;

import jakarta.servlet.http.HttpServletRequest;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ThymeleafTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThymeleafTest.class);

    private static int EARTH_RADIUS = 6371;

    @RequestMapping(value = "/index")
    public String index(Model model, HttpServletRequest request, @RequestParam(defaultValue = "0.0") double lat, @RequestParam(defaultValue = "0.0") double lon, @RequestParam(defaultValue = "0") int d) {
        model.addAttribute("settings", Settings.get());
        model.addAttribute("lat", lat);
        model.addAttribute("lon", lon);
        model.addAttribute("d", d);

        // Collect events
        LocalDateTime threshold = LocalDateTime.now().minusHours(2);
        Map<LocalDateTime, List<CalendarEvent>> startDateTimeToEventsMap = R.calendarEvent().findAll().stream()
                .filter(ce -> ce.startDateTime().isAfter(threshold))
                .filter(ce -> d == 0 || d > (int) calculateDistance(lat, lon, ce.calendarSource().calendarLocation().lat(), ce.calendarSource().calendarLocation().lon()))
                .sorted(Comparator.comparing(CalendarEvent::startDateTime))
                .collect(Collectors.groupingBy(ce -> ce.startDateTime()));
        model.addAttribute("startDateTimeToEventsMap", startDateTimeToEventsMap);

        return "index";
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
