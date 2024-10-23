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
public class CalendarSourceMultipleDaysScraper extends CalendarSourceScraperBaseHTML {

    public String type() {
        return "Multiple days";
    }

    @NotNull
    private String regex;
    static public final String REGEX = "regex";
    public String regex() {
        return regex;
    }
    public CalendarSourceMultipleDaysScraper regex(String v) {
        this.regex = v;
        return this;
    }

    @NotNull
    private String dateTimeLocale;
    static public final String DATETIMELOCALE = "dateTimeLocale";
    public String dateTimeLocale() {
        return dateTimeLocale;
    }
    public CalendarSourceMultipleDaysScraper dateTimeLocale(String v) {
        this.dateTimeLocale = v;
        return this;
    }

    @NotNull
    private String datePattern;
    static public final String DATEPATTERN = "datePattern";
    public String datePattern() {
        return datePattern;
    }
    public CalendarSourceMultipleDaysScraper datePattern(String v) {
        this.datePattern = v;
        return this;
    }

    @NotNull
    private String shortMonthNotation;
    static public final String SHORTMONTHNOTATION = "shortMonthNotation";
    public String shortMonthNotation() {
        return shortMonthNotation;
    }
    public CalendarSourceMultipleDaysScraper shortMonthNotation(String v) {
        this.shortMonthNotation = v;
        return this;
    }

    @NotNull
    private String timePattern;
    static public final String TIMEPATTERN = "timePattern";
    public String timePattern() {
        return timePattern;
    }
    public CalendarSourceMultipleDaysScraper timePattern(String v) {
        this.timePattern = v;
        return this;
    }

    @NotNull
    protected boolean nearestYear = false;
    static public final String NEARESTYEAR = "nearestYear";
    public boolean nearestYear() {
        return nearestYear;
    }
    public CalendarSourceMultipleDaysScraper nearestYear(boolean v) {
        this.nearestYear = v;
        return this;
    }

    private String startTimeDefault;
    static public final String STARTTIMEDEFAULT = "startTimeDefault";
    public String startTimeDefault() {
        return startTimeDefault;
    }
    public CalendarSourceMultipleDaysScraper startTimeDefault(String v) {
        this.startTimeDefault = v;
        return this;
    }

    private String endTimeDefault;
    static public final String ENDTIMEDEFAULT = "endTimeDefault";
    public String endTimeDefault() {
        return endTimeDefault;
    }
    public CalendarSourceMultipleDaysScraper endTimeDefault(String v) {
        this.endTimeDefault = v;
        return this;
    }

    @Override
    public List<CalendarEvent> generateEvents() {
        try {
            super.generateEvents();
            if (!isEnabled()) {
                return calendarEvents;
            }

            Locale locale = new Locale(dateTimeLocale);
           logAppend("Locale " + locale + "\n");

            String content = readScrapeUrlHTML();
            if (content.isBlank()) {
                status("No contents");
                return List.of();
            }
            content = sanatizeContent(content);

            DateTimeFormatter dateFormatter = createDateFormatter(datePattern, shortMonthNotation, locale);
            DateTimeFormatter timeFormatter = createTimeFormatter(timePattern, locale);
           logAppend(regex + "\n");
            Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(content);
            int lastMatchEnd = -1;
            while (matcher.find()) {
                logMatcher(matcher, content);

                // Extract strings
                String matchedString = content.substring(matcher.start(), matcher.end());
                String wholeString = content.substring(lastMatchEnd + 1, matcher.end());

                // Parse the match into a base day-month
                LocalDate localDate;
                if (nearestYear) {
                    MonthDay monthDay = MonthDay.parse(matchedString, dateFormatter);
                    localDate = determineDateByNearestYear(MonthDay.of(monthDay.getMonth(), monthDay.getDayOfMonth()));
                }
                else {
                   logAppend("Parsing '" + matchedString + "' with '" + datePattern + "'\n");
                    localDate = LocalDate.parse(matchedString, dateFormatter);
                }
                if (localDate == null) {
                   logAppend("Not able to determine a date for " + matchedString);
                    continue;
                }

                // Then scan al day notations
                wholeString = wholeString.replaceAll("[^#]*#", "");
                logAppend("> Cleaned up wholestring: " + wholeString + "\n");
                Matcher dayMatcher = Pattern.compile("[0-9][0-9]? ").matcher(wholeString);
                while (dayMatcher.find()) {
                    String matchedDayString = wholeString.substring(dayMatcher.start(), dayMatcher.end());
                    logAppend("---\n");
                    logAppend("> Start index: " + matcher.start() + "\n");
                    logAppend("> End index: " + matcher.end() + "\n");
                    logAppend("> Matched string: " + matchedDayString + "\n");
                    int dayOfMonth = Integer.parseInt(matchedDayString.trim());

                    // Create a date out of the matched number
                    LocalDate startLocalDate;
                    if (nearestYear) {
                        startLocalDate = determineDateByNearestYear(MonthDay.of(localDate.getMonth(), dayOfMonth));
                    }
                    else {
                        startLocalDate = LocalDate.of(localDate.getYear(), localDate.getMonth(), dayOfMonth);
                    }
                    if (startLocalDate == null) {
                       logAppend("Not able to determine a date for " + matchedDayString);
                        continue;
                    }

                    // Derive other values
                    LocalDate endLocalDate = startLocalDate;
                    LocalTime startLocalTime = LocalTime.parse(startTimeDefault(), timeFormatter);
                    LocalTime endLocalTime = LocalTime.parse(endTimeDefault(), timeFormatter);

                    LocalDateTime startLocalDateTime = LocalDateTime.of(startLocalDate, startLocalTime);
                   logAppend("startLocalDateTime: " + startLocalDateTime + "\n");

                    LocalDateTime endLocalDateTime = LocalDateTime.of(endLocalDate, endLocalTime);
                   logAppend("endLocalDateTime: " + endLocalDateTime + "\n");

                    endLocalDateTime = makeSureEndIsAfterStart(startLocalDateTime, endLocalDateTime);

                    // Create event
                    CalendarEvent calendarEvent = new CalendarEvent()
                            .subject("")
                            .startDateTime(startLocalDateTime)
                            .endDateTime(endLocalDateTime);
                    addCalendarEvent(calendarEvent);
                }
                lastMatchEnd = matcher.end();
            }
            dropExpiredEvents();
            sanatizeEvents();
           logAppend("Done\n");
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
}
