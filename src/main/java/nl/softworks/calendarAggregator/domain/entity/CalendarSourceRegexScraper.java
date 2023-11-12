package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
public class CalendarSourceRegexScraper extends CalendarSource {

    @NotNull
    private String content;
    static public final String CONTENT_PROPERTYID = "content";
    public String content() {
        return content;
    }
    public CalendarSourceRegexScraper content(String v) {
        this.content = v;
        return this;
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
    private int startTimeGroupIdx;
    static public final String STARTTIMEGROUPIDX_PROPERTYID = "startTimeGroupIdx";
    public int startTimeGroupIdx() {
        return startTimeGroupIdx;
    }
    public CalendarSourceRegexScraper startTimeGroupIdx(int v) {
        this.startTimeGroupIdx = v;
        return this;
    }

    @NotNull
    private int endTimeGroupIdx;
    static public final String ENDTIMEGROUPIDX_PROPERTYID = "endTimeGroupIdx";
    public int endTimeGroupIdx() {
        return startTimeGroupIdx;
    }
    public CalendarSourceRegexScraper endTimeGroupIdx(int v) {
        this.endTimeGroupIdx = v;
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
        calendarEvents.clear();
        Locale locale = new Locale(dateTimeLocale);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(datePattern, locale);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(timePattern, locale);

        String content = this.content.replace("\n", " ");
        if (stringBuilder != null) stringBuilder.append(content + "\n");
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            if (stringBuilder != null) stringBuilder.append("---\n");
            if (stringBuilder != null) stringBuilder.append("Start index: " + matcher.start() + "\n");
            if (stringBuilder != null) stringBuilder.append("End index: " + matcher.end() + "\n");
            for (int i = 0; i < matcher.groupCount() + 1; i++) {
                if (stringBuilder != null) stringBuilder.append(i + " " + matcher.group(i) + "\n");
            }

            String subject = matcher.group(subjectGroupIdx);
            String startDateString = matcher.group(startDateGroupIdx);
            String endDateString = matcher.group(endDateGroupIdx);
            String startTimeString = matcher.group(startTimeGroupIdx);
            String endTimeString = matcher.group(endTimeGroupIdx);

            LocalDate startLocalDate = LocalDate.parse(startDateString, dateFormatter);
            LocalDate endLocalDate = LocalDate.parse(endDateString, dateFormatter);
            LocalTime startLocalTime = LocalTime.parse(startTimeString, timeFormatter);
            LocalTime endLocalTime = LocalTime.parse(endTimeString, timeFormatter);

            CalendarEvent calendarEvent = new CalendarEvent()
                    .subject(subject)
                    .startDateTime(LocalDateTime.of(startLocalDate, startLocalTime))
                    .endDateTime(LocalDateTime.of(endLocalDate, endLocalTime));
            addCalendarEvent(calendarEvent);
        }
        return calendarEvents();
    }
}
