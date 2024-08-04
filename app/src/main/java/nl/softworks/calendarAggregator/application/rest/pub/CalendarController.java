package nl.softworks.calendarAggregator.application.rest.pub;

import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.Label;
import nl.softworks.calendarAggregator.domain.entity.Settings;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class CalendarController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarController.class);

    private static int EARTH_RADIUS = 6371;

    private void prepareTemplate(Model model, HttpServletRequest request, Double lat, Double lon, Integer distance, List<Label> labelInclude, List<Label> labelExclude) {
        model.addAttribute("settings", Settings.get());
        model.addAttribute("request", request);
        model.addAttribute("lat", lat);
        model.addAttribute("lon", lon);
        model.addAttribute("distance", distance);
        model.addAttribute("labels", R.label().findAllByOrderBySeqnrAsc());
        model.addAttribute("labelsInclude", labelInclude);
        model.addAttribute("labelsExclude", labelExclude);
        model.addAttribute("paramsQueryString", "lat=" + nullToEmpty(lat) + "&lon=" + nullToEmpty(lon) + "&distance=" + nullToEmpty(distance)
                + labelInclude.stream().map(l -> "&labelInclude=" + URLEncoder.encode(l.name(), StandardCharsets.UTF_8)).collect(Collectors.joining())
                + labelExclude.stream().map(l -> "&labelExclude=" + URLEncoder.encode(l.name(), StandardCharsets.UTF_8)).collect(Collectors.joining())
        );
    }

    // example http://localhost:8080/list
    @RequestMapping(value = {"/list", "/pub/html"}, produces = {"text/html"})
    public String index(Model model, HttpServletRequest request
            , @RequestParam(defaultValue = "") Double lat, @RequestParam(defaultValue = "") Double lon, @RequestParam(defaultValue = "") Integer distance
            , @RequestParam(defaultValue = "", name = "labelInclude") List<String> labelNamesInclude, @RequestParam(defaultValue = "", name = "labelExclude") List<String> labelNamesExclude) {

        List<Label> labelsInclude = labelsNameToEntities(labelNamesInclude);
        List<Label> labelsExclude = labelsNameToEntities(labelNamesExclude);
        prepareTemplate(model, request, lat, lon, distance, labelsInclude, labelsExclude);

        // Collect events
        List<CalendarEvent> events = R.calendarEvent().findAll().stream()
                .filter(ce -> filterEventOnDistance(ce, lat, lon, distance))
                .filter(ce -> filterEventOnLabels(ce, labelsInclude, labelsExclude))
                .toList();

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

        // labels
        Map<CalendarEvent, List<Label>> eventToLabels = events.stream()
                .map(ce -> Pair.of(ce, ce.labels()))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        model.addAttribute("eventToLabels", eventToLabels);
        model.addAttribute("labelGroups", R.labelGroup().findAllByOrderByNameAsc());

        return "list";
    }

    // example http://localhost:8080/month
    @RequestMapping(value = {"/", "/month", "/htmlmonth"}, produces = {"text/html"})
    public String month(Model model, HttpServletRequest request
            , @RequestParam(defaultValue = "") Double lat, @RequestParam(defaultValue = "") Double lon, @RequestParam(defaultValue = "") Integer distance
            , @RequestParam(defaultValue = "", name = "labelInclude") List<String> labelNamesInclude, @RequestParam(defaultValue = "", name = "labelExclude") List<String> labelNamesExclude
            , @RequestParam(defaultValue = "") Integer year, @RequestParam(defaultValue = "") Integer month
            , @RequestParam(defaultValue = "0") Integer moreWeeks) {

        List<Label> labelsInclude = labelsNameToEntities(labelNamesInclude);
        List<Label> labelsExclude = labelsNameToEntities(labelNamesExclude);
        prepareTemplate(model, request, lat, lon, distance, labelsInclude, labelsExclude);
        DateTimeFormatter yyyy = DateTimeFormatter.ofPattern("yyyy", Locale.ENGLISH);
        DateTimeFormatter mmm = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
        DateTimeFormatter dd = DateTimeFormatter.ofPattern("d", Locale.ENGLISH);
        DateTimeFormatter hhmm = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

        // Default parameter values
        if (year == null || month == null) {
            LocalDate now = LocalDate.now();
            year = now.getYear();
            month = now.getMonthValue();
            moreWeeks = now.getDayOfMonth() / 7;
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
        LocalDate renderEnd = monthEnd.plusDays(DayOfWeek.SUNDAY.getValue() - monthEnd.getDayOfWeek().getValue()).plusDays(moreWeeks * 7);

        // Split into weeks
        List<LocalDate> toBeRenderedDates = renderStart.datesUntil(renderEnd.plusDays(1)).toList();
        List<List<LocalDate>> weeksOfDates = Lists.partition(toBeRenderedDates, 7);
        model.addAttribute("weekOfDates", weeksOfDates);

        // Collect events
        List<CalendarEvent> events = R.calendarEvent().findAll().stream()
                .filter(ce -> filterEventOnDistance(ce, lat, lon, distance))
                .filter(ce -> filterEventOnLabels(ce, labelsInclude, labelsExclude))
                .toList();
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
                .map(ce -> Pair.of(ce, ce.labels()))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        model.addAttribute("eventToLabels", eventToLabels);
        model.addAttribute("labelGroups", R.labelGroup().findAllByOrderByNameAsc());

        // done
        model.addAttribute("eventStartDateTimeComparator", Comparator.comparing((CalendarEvent e) -> e.startDateTime()));
        return "month";
    }

    static boolean filterEventOnDistance(CalendarEvent ce, Double lat, Double lon, Integer d) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(2);
        return (ce.startDateTime().isAfter(threshold))
            && (lat == null || lon == null || d == null || d == 0 || d > (int) calculateDistance(lat, lon, ce.calendarSource().calendarLocation().lat(), ce.calendarSource().calendarLocation().lon()));
    }

    static boolean filterEventOnLabels(CalendarEvent ce, List<Label> labelsInclude, List<Label> labelsExclude) {
        List<Label> labels = ce.labels();
        for (Label label : labelsInclude) {
            if (!labels.contains(label)) {
                return false;
            }
        }
        for (Label label : labelsExclude) {
            if (labels.contains(label)) {
                return false;
            }
        }
        return true;
    }

    private <T> String nullToEmpty(T obj) {
        return obj == null ? "" : obj.toString();
    }

    static List<Label> labelsNameToEntities(List<String> names) {
        return names.stream()
                .map(l -> R.label().findByName(l).orElse(null))
                .filter(Objects::nonNull)
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
