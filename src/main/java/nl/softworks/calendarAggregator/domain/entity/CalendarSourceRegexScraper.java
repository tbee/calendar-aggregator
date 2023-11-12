package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;

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
    public int dateGroupIdx() {
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
    private String dateLocale;
    static public final String DATELOCALE_PROPERTYID = "dateLocale";
    public String dateLocale() {
        return dateLocale;
    }
    public CalendarSourceRegexScraper dateLocale(String v) {
        this.dateLocale = v;
        return this;
    }

    @Override
    public Set<CalendarEvent> generateEvents(StringBuilder stringBuilder) {
        calendarEvents.clear();

        String content = this.content.replace("\n", " ");
        stringBuilder.append(content + "\n");
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            stringBuilder.append("---\n");
            stringBuilder.append("Start index: " + matcher.start() + "\n");
            stringBuilder.append("End index: " + matcher.end() + "\n");
            for (int i = 0; i < matcher.groupCount() + 1; i++) {
                stringBuilder.append(i + " " + matcher.group(i) + "\n");
            }
        }
        return calendarEvents();
    }
}
