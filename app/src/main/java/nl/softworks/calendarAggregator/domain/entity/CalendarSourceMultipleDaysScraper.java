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
@DiscriminatorValue("multidays")
public class CalendarSourceMultipleDaysScraper extends CalendarSourceScraperBase {

    public String type() {
        return "Multiple days";
    }

    @NotNull
    private String regex;
    static public final String REGEX_PROPERTYID = "regex";
    public String regex() {
        return regex;
    }
    public CalendarSourceMultipleDaysScraper regex(String v) {
        this.regex = v;
        return this;
    }

    @NotNull
    private String dateTimeLocale;
    static public final String DATETIMELOCALE_PROPERTYID = "dateTimeLocale";
    public String dateTimeLocale() {
        return dateTimeLocale;
    }
    public CalendarSourceMultipleDaysScraper dateTimeLocale(String v) {
        this.dateTimeLocale = v;
        return this;
    }

    @NotNull
    private String datePattern;
    static public final String DATEPATTERN_PROPERTYID = "datePattern";
    public String datePattern() {
        return datePattern;
    }
    public CalendarSourceMultipleDaysScraper datePattern(String v) {
        this.datePattern = v;
        return this;
    }

    @NotNull
    private String shortMonthNotation;
    static public final String SHORTMONTHNOTATION_PROPERTYID = "shortMonthNotation";
    public String shortMonthNotation() {
        return shortMonthNotation;
    }
    public CalendarSourceMultipleDaysScraper shortMonthNotation(String v) {
        this.shortMonthNotation = v;
        return this;
    }

    @NotNull
    private String timePattern;
    static public final String TIMEPATTERN_PROPERTYID = "timePattern";
    public String timePattern() {
        return timePattern;
    }
    public CalendarSourceMultipleDaysScraper timePattern(String v) {
        this.timePattern = v;
        return this;
    }

    @NotNull
    protected boolean nearestYear = false;
    static public final String NEARESTYEAR_PROPERTYID = "nearestYear";
    public boolean nearestYear() {
        return nearestYear;
    }
    public CalendarSourceMultipleDaysScraper nearestYear(boolean v) {
        this.nearestYear = v;
        return this;
    }

    private String startTimeDefault;
    static public final String STARTTIMEDEFAULT_PROPERTYID = "startTimeDefault";
    public String startTimeDefault() {
        return startTimeDefault;
    }
    public CalendarSourceMultipleDaysScraper startTimeDefault(String v) {
        this.startTimeDefault = v;
        return this;
    }

    private String endTimeDefault;
    static public final String ENDTIMEDEFAULT_PROPERTYID = "endTimeDefault";
    public String endTimeDefault() {
        return endTimeDefault;
    }
    public CalendarSourceMultipleDaysScraper endTimeDefault(String v) {
        this.endTimeDefault = v;
        return this;
    }

    @Override
    public List<CalendarEvent> generateEvents(StringBuilder stringBuilder) {
        try {
            calendarEvents.removeIf(ce -> ce.generated);

            status("");
            Locale locale = new Locale(dateTimeLocale);
            if (stringBuilder != null) stringBuilder.append("Locale ").append(locale).append("\n");

            String content = readScrapeUrl(stringBuilder);
            if (content.isBlank()) {
                status("No contents");
                return List.of();
            }
            content = sanatizeContent(content, stringBuilder);

            DateTimeFormatter dateFormatter = createDateFormatter(datePattern, shortMonthNotation, locale, stringBuilder);
            DateTimeFormatter timeFormatter = createTimeFormatter(timePattern, locale, stringBuilder);
            if (stringBuilder != null) stringBuilder.append(regex).append("\n");
            Matcher matcher = Pattern.compile(regex).matcher(content);
            int lastMatchEnd = -1;
            while (matcher.find()) {
                String matchedString = content.substring(matcher.start(), matcher.end());
                String wholeString = content.substring(lastMatchEnd + 1, matcher.end());
                if (stringBuilder != null) {
                    stringBuilder.append("---\n");
                    stringBuilder.append("Start index: ").append(matcher.start()).append("\n");
                    stringBuilder.append("End index: ").append(matcher.end()).append("\n");
                    stringBuilder.append("Matched string: ").append(matchedString).append("\n");
                    stringBuilder.append("Whole string: ").append(wholeString).append("\n");
                    for (int i = 0; i < matcher.groupCount() + 1; i++) {
                        stringBuilder.append("Group ").append(i).append(" = ").append(matcher.group(i)).append("\n");
                    }
                }

                // Parse the match into a base day-month
                LocalDate localDate;
                if (nearestYear) {
                    MonthDay monthDay = MonthDay.parse(matchedString, dateFormatter);
                    localDate = determineDateByNearestYear(MonthDay.of(monthDay.getMonth(), monthDay.getDayOfMonth()));
                }
                else {
                    localDate = LocalDate.parse(matchedString, dateFormatter);
                }

                // Then scan al day notations
                Matcher dayMatcher = Pattern.compile("[0-9][0-9]? ").matcher(wholeString);
                while (dayMatcher.find()) {
                    String matchedDayString = wholeString.substring(dayMatcher.start(), dayMatcher.end());
                    if (stringBuilder != null) {
                        stringBuilder.append("---\n");
                        stringBuilder.append("> Start index: ").append(matcher.start()).append("\n");
                        stringBuilder.append("> End index: ").append(matcher.end()).append("\n");
                        stringBuilder.append("> Matched string: ").append(matchedDayString).append("\n");
                    }
                    int dayOfMonth = Integer.parseInt(matchedDayString.trim());

                    LocalDate startLocalDate;
                    if (nearestYear) {
                        startLocalDate = determineDateByNearestYear(MonthDay.of(localDate.getMonth(), dayOfMonth));
                    }
                    else {
                        startLocalDate = LocalDate.of(localDate.getYear(), localDate.getMonth(), dayOfMonth);
                    }
                    LocalDate endLocalDate = startLocalDate;
                    LocalTime startLocalTime = LocalTime.parse(startTimeDefault(), timeFormatter);
                    LocalTime endLocalTime = LocalTime.parse(endTimeDefault(), timeFormatter);

                    LocalDateTime startLocalDateTime = LocalDateTime.of(startLocalDate, startLocalTime);
                    if (stringBuilder != null) stringBuilder.append("startLocalDateTime: ").append(startLocalDateTime).append("\n");
                    LocalDateTime endLocalDateTime = LocalDateTime.of(endLocalDate, endLocalTime);
                    if (stringBuilder != null) stringBuilder.append("endLocalDateTime: ").append(endLocalDateTime).append("\n");
                    if (endLocalDateTime.isBefore(startLocalDateTime)) {
                        endLocalDateTime = endLocalDateTime.plusDays(1); // This is to correct an end time that is on or after midnight
                        if (stringBuilder != null) stringBuilder.append("End moment < start moment, added one day: ").append(endLocalDateTime).append("\n");
                    }

                    CalendarEvent calendarEvent = new CalendarEvent()
                            .subject("")
                            .startDateTime(startLocalDateTime)
                            .endDateTime(endLocalDateTime);
                    addCalendarEvent(calendarEvent);
                }
                lastMatchEnd = matcher.end();
            }
            if (stringBuilder != null) stringBuilder.append("Done\n");
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
}
