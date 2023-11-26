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
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@DiscriminatorValue("regex")
public class CalendarSourceRegexScraper extends CalendarSourceScraperBase {

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
    private int yearDefault;
    static public final String YEARDEFAULT_PROPERTYID = "yearDefault";
    public int yearDefault() {
        return yearDefault;
    }
    public CalendarSourceRegexScraper yearDefault(int v) {
        this.yearDefault = v;
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
        return startTimeGroupIdx;
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
            super.generateEvents(stringBuilder);

            status("");
            Locale locale = new Locale(dateTimeLocale);
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(datePattern, locale);
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(timePattern, locale);

            if (scrapeUrl != null && !scrapeUrl.isBlank()) {
                String content = readScrapeUrl(stringBuilder);
                content(content);
                if (content.isBlank()) {
                    status("No contents");
                    return List.of();
                }
            }

            String content = sanatize(this.content(), stringBuilder);
            if (stringBuilder != null) stringBuilder.append(regex).append("\n");
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                if (stringBuilder != null) stringBuilder.append("---\n");
                if (stringBuilder != null) stringBuilder.append("Start index: ").append(matcher.start()).append("\n");
                if (stringBuilder != null) stringBuilder.append("End index: ").append(matcher.end()).append("\n");
                for (int i = 0; i < matcher.groupCount() + 1; i++) {
                    if (stringBuilder != null) stringBuilder.append("Idx ").append(i).append(" = ").append(matcher.group(i)).append("\n");
                }

                String subject = subjectGroupIdx < 1 ? "" : matcher.group(subjectGroupIdx);
                String startDateString = matcher.group(startDateGroupIdx);
                String endDateString = matcher.group(endDateGroupIdx);
                String startTimeString = startTimeGroupIdx < 1 ? startTimeDefault : matcher.group(startTimeGroupIdx);
                String endTimeString = endTimeGroupIdx < 1 ? endTimeDefault : matcher.group(endTimeGroupIdx);

                LocalDate startLocalDate;
                if (yearDefault == 0) {
                    startLocalDate = LocalDate.parse(startDateString, dateFormatter);
                } else {
                    MonthDay monthDay = MonthDay.parse(startDateString, dateFormatter);
                    startLocalDate = LocalDate.of(yearDefault, monthDay.getMonth(), monthDay.getDayOfMonth());
                }
                LocalDate endLocalDate;
                if (yearDefault == 0) {
                    endLocalDate = LocalDate.parse(endDateString, dateFormatter);
                } else {
                    MonthDay monthDay = MonthDay.parse(endDateString, dateFormatter);
                    endLocalDate = LocalDate.of(yearDefault, monthDay.getMonth(), monthDay.getDayOfMonth());
                }
                LocalTime startLocalTime = LocalTime.parse(startTimeString, timeFormatter);
                LocalTime endLocalTime = LocalTime.parse(endTimeString, timeFormatter);

                CalendarEvent calendarEvent = new CalendarEvent()
                        .subject(subject)
                        .startDateTime(LocalDateTime.of(startLocalDate, startLocalTime))
                        .endDateTime(LocalDateTime.of(endLocalDate, endLocalTime));
                addCalendarEvent(calendarEvent);
            }
            if (stringBuilder != null) stringBuilder.append("Done\n");
            if (calendarEvents().size() == 0) {
                status("No events are generated");
                return List.of();
            }
            status("ok");
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
}
