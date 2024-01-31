package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@DiscriminatorValue("regex")
public class CalendarSourceRegexScraper extends CalendarSourceScraperBase {

    public String type() {
        return "Regex";
    }

    @NotNull
    private String regex;
    static public final String REGEX_PROPERTYID = "regex";
    public String regex() {
        return regex;
    }
    public CalendarSourceRegexScraper regex(String v) {
        this.regex = v;
        return this;
    }

    @NotNull
    private int subjectGroupIdx;
    static public final String SUBJECTGROUPIDX_PROPERTYID = "subjectGroupIdx";
    public int subjectGroupIdx() {
        return subjectGroupIdx;
    }
    public CalendarSourceRegexScraper subjectGroupIdx(int v) {
        this.subjectGroupIdx = v;
        return this;
    }

    @NotNull
    private int startDateGroupIdx;
    static public final String STARTDATEGROUPIDX_PROPERTYID = "startDateGroupIdx";
    public int startDateGroupIdx() {
        return startDateGroupIdx;
    }
    public CalendarSourceRegexScraper startDateGroupIdx(int v) {
        this.startDateGroupIdx = v;
        return this;
    }

    @NotNull
    private int endDateGroupIdx;
    static public final String ENDDATEGROUPIDX_PROPERTYID = "endDateGroupIdx";
    public int endDateGroupIdx() {
        return startDateGroupIdx;
    }
    public CalendarSourceRegexScraper endDateGroupIdx(int v) {
        this.endDateGroupIdx = v;
        return this;
    }

    @NotNull
    private String datePattern;
    static public final String DATEPATTERN_PROPERTYID = "datePattern";
    public String datePattern() {
        return datePattern;
    }
    public CalendarSourceRegexScraper datePattern(String v) {
        this.datePattern = v;
        return this;
    }

    @NotNull
    private String shortMonthNotation;
    static public final String SHORTMONTHNOTATION_PROPERTYID = "shortMonthNotation";
    public String shortMonthNotation() {
        return shortMonthNotation;
    }
    public CalendarSourceRegexScraper shortMonthNotation(String v) {
        this.shortMonthNotation = v;
        return this;
    }

    @NotNull
    protected boolean nearestYear = false;
    static public final String NEARESTYEAR_PROPERTYID = "nearestYear";
    public boolean nearestYear() {
        return nearestYear;
    }
    public CalendarSourceRegexScraper nearestYear(boolean v) {
        this.nearestYear = v;
        return this;
    }

    private int startTimeGroupIdx;
    static public final String STARTTIMEGROUPIDX_PROPERTYID = "startTimeGroupIdx";
    public int startTimeGroupIdx() {
        return startTimeGroupIdx;
    }
    public CalendarSourceRegexScraper startTimeGroupIdx(int v) {
        this.startTimeGroupIdx = v;
        return this;
    }

    private String startTimeDefault;
    static public final String STARTTIMEDEFAULT_PROPERTYID = "startTimeDefault";
    public String startTimeDefault() {
        return startTimeDefault;
    }
    public CalendarSourceRegexScraper startTimeDefault(String v) {
        this.startTimeDefault = v;
        return this;
    }

    private int endTimeGroupIdx;
    static public final String ENDTIMEGROUPIDX_PROPERTYID = "endTimeGroupIdx";
    public int endTimeGroupIdx() {
        return endTimeGroupIdx;
    }
    public CalendarSourceRegexScraper endTimeGroupIdx(int v) {
        this.endTimeGroupIdx = v;
        return this;
    }

    private String endTimeDefault;
    static public final String ENDTIMEDEFAULT_PROPERTYID = "endTimeDefault";
    public String endTimeDefault() {
        return endTimeDefault;
    }
    public CalendarSourceRegexScraper endTimeDefault(String v) {
        this.endTimeDefault = v;
        return this;
    }

    @NotNull
    private String timePattern;
    static public final String TIMEPATTERN_PROPERTYID = "timePattern";
    public String timePattern() {
        return timePattern;
    }
    public CalendarSourceRegexScraper timePattern(String v) {
        this.timePattern = v;
        return this;
    }

    @NotNull
    private String dateTimeLocale;
    static public final String DATETIMELOCALE_PROPERTYID = "dateTimeLocale";
    public String dateTimeLocale() {
        return dateTimeLocale;
    }
    public CalendarSourceRegexScraper dateTimeLocale(String v) {
        this.dateTimeLocale = v;
        return this;
    }

    @Override
    public List<CalendarEvent> generateEvents(StringBuilder stringBuilder) {
        try {
            status("");

            // Remove all generated events (keep the manual ones)
            calendarEvents.removeIf(ce -> ce.generated);

            // Create formatters
            Locale locale = new Locale(dateTimeLocale);
            if (stringBuilder != null) stringBuilder.append("Locale ").append(locale).append("\n");
            DateTimeFormatter dateFormatter = createDateFormatter(datePattern, shortMonthNotation, locale, stringBuilder);
            DateTimeFormatter timeFormatter = createTimeFormatter(timePattern, locale, stringBuilder);

            // Get contents
            String content = readScrapeUrl(stringBuilder);
            if (content.isBlank()) {
                status("No contents");
                return List.of();
            }
            content = sanatizeContent(content, stringBuilder);

            // Apply regex
            if (stringBuilder != null) stringBuilder.append(regex).append("\n");
            Matcher matcher = Pattern.compile(regex).matcher(content);
            while (matcher.find()) {
                logMatcherInStringBuilder(matcher, content, stringBuilder);

                // extract strings
                String subject = subjectGroupIdx < 1 ? "" : matcher.group(subjectGroupIdx);
                String startDateString = matcher.group(startDateGroupIdx);
                String endDateString = matcher.group(endDateGroupIdx);
                String startTimeString = startTimeGroupIdx < 1 ? startTimeDefault : matcher.group(startTimeGroupIdx);
                String endTimeString = endTimeGroupIdx < 1 ? endTimeDefault : matcher.group(endTimeGroupIdx);

                try {
                    LocalDate startLocalDate = parseLocalDate(startDateString, dateFormatter, stringBuilder);
                    if (startLocalDate == null) {
                        if (stringBuilder != null) stringBuilder.append("Not able to determine a startdate for ").append(startDateString);
                        continue;
                    }

                    LocalDate endLocalDate = parseLocalDate(endDateString, dateFormatter, stringBuilder);
                    if (endLocalDate == null) {
                        if (stringBuilder != null) stringBuilder.append("Not able to determine an enddate for ").append(endDateString);
                        continue;
                    }

                    LocalTime startLocalTime = parseLocalTime(startTimeString, timeFormatter, stringBuilder);
                    if (startLocalTime == null) {
                        if (stringBuilder != null) stringBuilder.append("Not able to determine a starttime for ").append(startTimeString);
                        continue;
                    }

                    LocalTime endLocalTime = parseLocalTime(endTimeString, timeFormatter, stringBuilder);
                    if (endLocalTime == null) {
                        if (stringBuilder != null) stringBuilder.append("Not able to determine an endtime for ").append(endTimeString);
                        continue;
                    }

                    LocalDateTime startLocalDateTime = LocalDateTime.of(startLocalDate, startLocalTime);
                    if (stringBuilder != null) stringBuilder.append("startLocalDateTime: ").append(startLocalDateTime).append("\n");

                    LocalDateTime endLocalDateTime = LocalDateTime.of(endLocalDate, endLocalTime);
                    if (stringBuilder != null) stringBuilder.append("endLocalDateTime: ").append(endLocalDateTime).append("\n");

                    endLocalDateTime = makeSureEndIsAfterStart(startLocalDateTime, endLocalDateTime, stringBuilder);

                    // Create event
                    CalendarEvent calendarEvent = new CalendarEvent()
                            .subject(subject)
                            .startDateTime(startLocalDateTime)
                            .endDateTime(endLocalDateTime);
                    addCalendarEvent(calendarEvent);
                }
                catch (DateTimeParseException e) {
                    if (stringBuilder != null) {
                        try {
                            String example = LocalDate.of(2023, 12, 31).format(dateFormatter);
                            stringBuilder.append("Date example: ").append(example).append("\n");
                        } catch (RuntimeException e2) {
                            // ignore
                        }

                        try {
                            String example = LocalTime.of(12, 23, 45).format(timeFormatter);
                            stringBuilder.append("Time example: ").append(example).append("\n");
                        } catch (RuntimeException e2) {
                            // ignore
                        }
                    }
                    throw e;
                }
            }
            if (stringBuilder != null) stringBuilder.append("Done\n");

            // set status
            if (calendarEvents().isEmpty()) {
                status("No events");
                return List.of();
            }
            status(OK);

            return calendarEvents();
        }
        catch (RuntimeException e) {
            status(e.getMessage());
            if (stringBuilder != null) {
                StringWriter stringWriter = new StringWriter();
                e.printStackTrace(new PrintWriter(stringWriter));
                stringBuilder.append(stringWriter.toString());
            }
            throw e;
        }
    }

    private LocalTime parseLocalTime(String timeString, DateTimeFormatter timeFormatter, StringBuilder stringBuilder) {
        if (stringBuilder != null) stringBuilder.append("Parsing '").append(timeString).append("' with '").append(timePattern).append("'\n");
        return LocalTime.parse(timeString, timeFormatter);
    }

    private LocalDate parseLocalDate(String dateString, DateTimeFormatter dateFormatter, StringBuilder stringBuilder) {
        LocalDate localDate;
        if (nearestYear) {
            MonthDay monthDay = MonthDay.parse(dateString, dateFormatter);
            localDate = determineDateByNearestYear(monthDay, stringBuilder);
        } else {
            if (stringBuilder != null) stringBuilder.append("Parsing '").append(dateString).append("' with '").append(datePattern).append("'\n");
            localDate = LocalDate.parse(dateString, dateFormatter);
        }
        return localDate;
    }
}
