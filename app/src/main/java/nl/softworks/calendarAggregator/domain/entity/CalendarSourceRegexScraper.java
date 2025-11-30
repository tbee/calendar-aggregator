package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
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
public class CalendarSourceRegexScraper extends CalendarSourceScraperBaseHTML {

    public String type() {
        return "Regex";
    }

    @NotNull
    private String regex;
    static public final String REGEX = "regex";
    public String regex() {
        return regex;
    }
    public CalendarSourceRegexScraper regex(String v) {
        this.regex = v;
        return this;
    }

    @NotNull
    private int subjectGroupIdx;
    static public final String SUBJECTGROUPIDX = "subjectGroupIdx";
    public int subjectGroupIdx() {
        return subjectGroupIdx;
    }
    public CalendarSourceRegexScraper subjectGroupIdx(int v) {
        this.subjectGroupIdx = v;
        return this;
    }

    @NotNull
    private int startDateGroupIdx;
    static public final String STARTDATEGROUPIDX = "startDateGroupIdx";
    public int startDateGroupIdx() {
        return startDateGroupIdx;
    }
    public CalendarSourceRegexScraper startDateGroupIdx(int v) {
        this.startDateGroupIdx = v;
        return this;
    }

    @NotNull
    private int endDateGroupIdx;
    static public final String ENDDATEGROUPIDX = "endDateGroupIdx";
    public int endDateGroupIdx() {
        return startDateGroupIdx;
    }
    public CalendarSourceRegexScraper endDateGroupIdx(int v) {
        this.endDateGroupIdx = v;
        return this;
    }

    @NotNull
    private String datePattern;
    static public final String DATEPATTERN = "datePattern";
    public String datePattern() {
        return datePattern;
    }
    public CalendarSourceRegexScraper datePattern(String v) {
        this.datePattern = v;
        return this;
    }

    @NotNull
    private String shortMonthNotation;
    static public final String SHORTMONTHNOTATION = "shortMonthNotation";
    public String shortMonthNotation() {
        return shortMonthNotation;
    }
    public CalendarSourceRegexScraper shortMonthNotation(String v) {
        this.shortMonthNotation = v;
        return this;
    }

    @NotNull
    protected boolean nearestYear = false;
    static public final String NEARESTYEAR = "nearestYear";
    public boolean nearestYear() {
        return nearestYear;
    }
    public CalendarSourceRegexScraper nearestYear(boolean v) {
        this.nearestYear = v;
        return this;
    }

    private int startTimeGroupIdx;
    static public final String STARTTIMEGROUPIDX = "startTimeGroupIdx";
    public int startTimeGroupIdx() {
        return startTimeGroupIdx;
    }
    public CalendarSourceRegexScraper startTimeGroupIdx(int v) {
        this.startTimeGroupIdx = v;
        return this;
    }

    private String startTimeDefault;
    static public final String STARTTIMEDEFAULT = "startTimeDefault";
    public String startTimeDefault() {
        return startTimeDefault;
    }
    public CalendarSourceRegexScraper startTimeDefault(String v) {
        this.startTimeDefault = v;
        return this;
    }

    private int endTimeGroupIdx;
    static public final String ENDTIMEGROUPIDX = "endTimeGroupIdx";
    public int endTimeGroupIdx() {
        return endTimeGroupIdx;
    }
    public CalendarSourceRegexScraper endTimeGroupIdx(int v) {
        this.endTimeGroupIdx = v;
        return this;
    }

    private String endTimeDefault;
    static public final String ENDTIMEDEFAULT = "endTimeDefault";
    public String endTimeDefault() {
        return endTimeDefault;
    }
    public CalendarSourceRegexScraper endTimeDefault(String v) {
        this.endTimeDefault = v;
        return this;
    }

    @NotNull
    private String timePattern;
    static public final String TIMEPATTERN = "timePattern";
    public String timePattern() {
        return timePattern;
    }
    public CalendarSourceRegexScraper timePattern(String v) {
        this.timePattern = v;
        return this;
    }

    @NotNull
    private String dateTimeLocale;
    static public final String DATETIMELOCALE = "dateTimeLocale";
    public String dateTimeLocale() {
        return dateTimeLocale;
    }
    public CalendarSourceRegexScraper dateTimeLocale(String v) {
        this.dateTimeLocale = v;
        return this;
    }

    @Override
    public List<CalendarEvent> generateEvents() {
        try {
            super.generateEvents();
            if (!isEnabled()) {
                return calendarEvents;
            }

            // Create formatters
            Locale locale = Locale.of(dateTimeLocale);
            logAppend("Locale " + locale + "\n");
            DateTimeFormatter dateFormatter = createDateFormatter(datePattern, shortMonthNotation, locale);
            DateTimeFormatter timeFormatter = createTimeFormatter(timePattern, locale);

            // Get contents
            String content = readScrapeUrlHTML();
            if (content.isBlank()) {
                status("No contents");
                return List.of();
            }
            content = sanatizeContent(content);

            // Apply regex
            logAppend(regex + "\n");
            Pattern pattern = (caseInsensitive ? Pattern.compile(regex, Pattern.CASE_INSENSITIVE) : Pattern.compile(regex));
            Matcher matcher = pattern.matcher(content);
            String previousSubject = "";
            while (matcher.find()) {
                logMatcher(matcher, content);

                // extract strings
                String subject = subjectGroupIdx < 1 ? "" : matcher.group(subjectGroupIdx);
                if (subject == null) {
                    subject = previousSubject;
                    logAppend("Subject is null, using previous subject: " + subject + "\n");
                }
                previousSubject = subject;
                String startDateString = matcher.group(startDateGroupIdx);
                String endDateString = matcher.group(endDateGroupIdx);
                String startTimeString = startTimeGroupIdx < 1 ? startTimeDefault : matcher.group(startTimeGroupIdx);
                String endTimeString = endTimeGroupIdx < 1 ? endTimeDefault : matcher.group(endTimeGroupIdx);

                try {
                    LocalDate startLocalDate = parseLocalDate(startDateString, dateFormatter);
                    if (startLocalDate == null) {
                        logAppend("Not able to determine a startdate for " + startDateString);
                        continue;
                    }

                    LocalDate endLocalDate = parseLocalDate(endDateString, dateFormatter);
                    if (endLocalDate == null) {
                        logAppend("Not able to determine an enddate for " + endDateString);
                        continue;
                    }

                    LocalTime startLocalTime = parseLocalTime(startTimeString != null ? startTimeString : startTimeDefault, timeFormatter);
                    if (startLocalTime == null) {
                        logAppend("Not able to determine a starttime for " + startTimeString);
                        continue;
                    }

                    LocalTime endLocalTime = null;
                    if (endTimeString != null && endTimeString.startsWith("+")) {
                        LocalTime endLocalTimeAddition = parseLocalTime(endTimeString.substring(1), timeFormatter);
                        Duration duration = Duration.between(LocalTime.of(0, 0, 0), endLocalTimeAddition);
                        endLocalTime = startLocalTime.plus(duration);
                    }
                    else {
                        endLocalTime = parseLocalTime(endTimeString != null ? endTimeString : endTimeDefault, timeFormatter);
                    }
                    if (endLocalTime == null) {
                        logAppend("Not able to determine an endtime for " + endTimeString);
                        continue;
                    }

                    LocalDateTime startLocalDateTime = LocalDateTime.of(startLocalDate, startLocalTime);
                    logAppend("startLocalDateTime: " + startLocalDateTime + "\n");

                    LocalDateTime endLocalDateTime = LocalDateTime.of(endLocalDate, endLocalTime);
                    logAppend("endLocalDateTime: " + endLocalDateTime + "\n");

                    endLocalDateTime = makeSureEndIsAfterStart(startLocalDateTime, endLocalDateTime);

                    // Create event
                    CalendarEvent calendarEvent = new CalendarEvent()
                            .subject(subject)
                            .startDateTime(startLocalDateTime)
                            .endDateTime(endLocalDateTime);
                    addCalendarEvent(calendarEvent);
                }
                catch (DateTimeParseException e) {
                    try {
                        String example = LocalDate.of(2023, 12, 31).format(dateFormatter);
                         logAppend("Date example: " + example + "\n");
                    } catch (RuntimeException e2) {
                        // ignore
                    }

                    try {
                        String example = LocalTime.of(12, 23, 45).format(timeFormatter);
                         logAppend("Time example: " + example + "\n");
                    } catch (RuntimeException e2) {
                        // ignore
                    }
                    throw e;
                }
            }
            dropExpiredEvents();
            sanatizeEvents();
            logAppend("Done\n");

            // set status
            if (calendarEvents().isEmpty()) {
                status("No events");
                return List.of();
            }

            return calendarEvents();
        }
        catch (RuntimeException e) {
            status(e.getMessage());
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            logAppend(stringWriter.toString());
            throw e;
        }
    }

    private LocalTime parseLocalTime(String timeString, DateTimeFormatter timeFormatter) {
        logAppend("Parsing '" + timeString + "' with '" + timePattern + "'\n");
        return LocalTime.parse(timeString, timeFormatter);
    }

    private LocalDate parseLocalDate(String dateString, DateTimeFormatter dateFormatter) {
        LocalDate localDate;
        if (nearestYear) {
            MonthDay monthDay = MonthDay.parse(dateString, dateFormatter);
            localDate = determineDateByNearestYear(monthDay);
        } else {
            logAppend("Parsing '" + dateString + "' with '" + datePattern + "'\n");
            localDate = LocalDate.parse(dateString, dateFormatter);
        }
        return localDate;
    }
}
