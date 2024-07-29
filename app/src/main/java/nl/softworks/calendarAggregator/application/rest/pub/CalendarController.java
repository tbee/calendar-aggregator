package nl.softworks.calendarAggregator.application.rest.pub;

import com.google.common.collect.Lists;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceLabelAssignment;
import nl.softworks.calendarAggregator.domain.entity.Label;
import nl.softworks.calendarAggregator.domain.entity.Settings;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class CalendarController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarController.class);

    private static int EARTH_RADIUS = 6371;

    private void prepareTemplate(Model model, Double lat, Double lon, Integer distance) {
        model.addAttribute("settings", Settings.get());
        model.addAttribute("lat", lat);
        model.addAttribute("lon", lon);
        model.addAttribute("distance", distance);
    }

    @RequestMapping(value = "/index")
    public String index(Model model, @RequestParam(defaultValue = "") Double lat, @RequestParam(defaultValue = "") Double lon, @RequestParam(defaultValue = "") Integer distance) {
        prepareTemplate(model, lat, lon, distance);

        // Collect events
        List<CalendarEvent> events = filterEventsOnDistance(model, lat, lon, distance);

        // List
        Map<LocalDate, List<CalendarEvent>> dateToEvents = events.stream()
                .collect(Collectors.groupingBy(ce -> ce.startDateTime().toLocalDate()));
        model.addAttribute("dateToEvents", dateToEvents);
        model.addAttribute("eventStartDateTimeComparator", Comparator.comparing((CalendarEvent e) -> e.startDateTime()));

        // When
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("E yyyy-MM-dd HH:mm");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        Map<CalendarEvent, String> eventToWhen = events.stream()
                .map(event -> Pair.of(event,
                        dateTimeFormatter.format(event.startDateTime())
                                + " - "
                                + (event.startDateTime().toLocalDate().equals(event.endDateTime().toLocalDate()) ? timeFormatter : dateTimeFormatter).format(event.endDateTime())))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        model.addAttribute("eventToWhen", eventToWhen);

        // What
        Map<CalendarEvent, String> eventToWhat = events.stream()
                .map(event -> Pair.of(event, event.calendarSource().calendarLocation().name() + (event.subject().isBlank() ? "" : " - " + event.subject())))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        model.addAttribute("eventToWhat", eventToWhat);

        return "list";
    }

    @RequestMapping(value = "/month2")
    public String month(Model model, @RequestParam(defaultValue = "") Double lat, @RequestParam(defaultValue = "") Double lon, @RequestParam(defaultValue = "") Integer distance
            , @RequestParam(defaultValue = "") Integer year, @RequestParam(defaultValue = "") Integer month) {
        prepareTemplate(model, lat, lon, distance);
        DateTimeFormatter yyyy = DateTimeFormatter.ofPattern("yyyy", Locale.ENGLISH);
        DateTimeFormatter mmm = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
        DateTimeFormatter dd = DateTimeFormatter.ofPattern("d", Locale.ENGLISH);
        DateTimeFormatter hhmm = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

        // Default parameter values
        if (year == null || month == null) {
            LocalDate now = LocalDate.now();
            year = now.getYear();
            month = now.getMonthValue();
        }
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
        LocalDate prevMonth = monthStart.minusMonths(1);
        LocalDate nextMonth = monthStart.plusMonths(1);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("year", yyyy.format(monthStart));
        model.addAttribute("month", mmm.format(monthStart));
        model.addAttribute("prevyear", "" + prevMonth.getYear());
        model.addAttribute("prevmonth", "" + prevMonth.getMonthValue());
        model.addAttribute("nextyear", "" + nextMonth.getYear());
        model.addAttribute("nextmonth", "" + nextMonth.getMonthValue());

        // start at monday
        LocalDate renderStart = monthStart.minusDays(monthStart.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
        LocalDate renderEnd = monthEnd.plusDays(DayOfWeek.SUNDAY.getValue() - monthEnd.getDayOfWeek().getValue());

        // Split into weeks
        List<LocalDate> toBeRenderedDates = renderStart.datesUntil(renderEnd.plusDays(1)).toList();
        List<List<LocalDate>> weeksOfDates = Lists.partition(toBeRenderedDates, 7);
        model.addAttribute("weekOfDates", weeksOfDates);

        // Collect events
        List<CalendarEvent> events = filterEventsOnDistance(model, lat, lon, distance);
        Map<LocalDate, List<CalendarEvent>> dateToEventsWithPossibleEmptyDates = events.stream()
                .filter(ce -> !ce.startDateTime().toLocalDate().isAfter(renderEnd))
                .filter(ce -> !ce.endDateTime().toLocalDate().isBefore(renderStart))
                .collect(Collectors.groupingBy(ce -> ce.startDateTime().toLocalDate()));
        Map<LocalDate, List<CalendarEvent>> dateToEvents = toBeRenderedDates.stream()
                .map(date -> Pair.of(date, dateToEventsWithPossibleEmptyDates.get(date) == null ? new ArrayList<CalendarEvent>() : dateToEventsWithPossibleEmptyDates.get(date)))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        model.addAttribute("dateToEvents", dateToEvents);

        // Is a date inside or outside the month
        Map<LocalDate, Boolean> dateIsOutsideMonth = toBeRenderedDates.stream()
                .map(date -> Pair.of(date, date.isBefore(monthStart) || date.isAfter(monthEnd)))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        model.addAttribute("dateIsOutsideMonth", dateIsOutsideMonth);

        // The text of an event
        Map<CalendarEvent, String> eventToText = events.stream()
                .map(ce -> Pair.of(ce, hhmm.format(ce.startDateTime()) + " " + ce.calendarSource().calendarLocation().name()))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        model.addAttribute("eventToText", eventToText);

        // The description of an event
        Map<CalendarEvent, String> eventToTooltip = events.stream()
                .map(ce -> Pair.of(ce, (ce.determineSubject().isBlank() ? "See the website" : ce.determineSubject())))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        model.addAttribute("eventToTooltip", eventToTooltip);

        // labels
        Map<CalendarEvent, List<Label>> eventToLabels = events.stream()
                .map(ce -> {
                    String description = ce.determineSubject();
                    List<Label> labels = ce.calendarSource().labelAssignments().stream()
                            .filter(la -> la.subjectRegexp().isBlank() || description.matches(la.subjectRegexp()))
                            .map(CalendarSourceLabelAssignment::label)
                            .toList();
                    return Pair.of(ce, labels);
                })
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        model.addAttribute("eventToLabels", eventToLabels);

        // done
        model.addAttribute("eventStartDateTimeComparator", Comparator.comparing((CalendarEvent e) -> e.startDateTime()));
        return "month";
    }

    static List<CalendarEvent> filterEventsOnDistance(Model model, Double lat, Double lon, Integer d) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(2);
        return R.calendarEvent().findAll().stream()
                .filter(ce -> ce.startDateTime().isAfter(threshold))
                .filter(ce -> lat == null || lon == null || d == null || d == 0 || d > (int) calculateDistance(lat, lon, ce.calendarSource().calendarLocation().lat(), ce.calendarSource().calendarLocation().lon()))
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
