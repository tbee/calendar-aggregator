package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import org.json.JSONObject;
import org.json.XML;

import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@Entity
@DiscriminatorValue("json")
public class CalendarSourceXmlScraper extends CalendarSourceScraperBase {

    public String type() {
        return "Xml";
    }

    @NotNull
    private boolean jsonToXml = false;
    static public final String JSONTOXML_PROPERTYID = "jsonToXml";
    public boolean jsonToXml() {
        return jsonToXml;
    }
    public CalendarSourceXmlScraper jsonToXml(boolean v) {
        this.jsonToXml = v;
        return this;
    }

    @NotNull
    private String xpath = "";
    static public final String XPATH_PROPERTYID = "xpath";
    public String xpath() {
        return xpath;
    }
    public CalendarSourceXmlScraper xpath(String v) {
        this.xpath = v;
        return this;
    }

    @NotNull
    private String startdateXpath = "";
    static public final String STARTDATEXPATH_PROPERTYID = "startdateXpath";
    public String startdateXpath() {
        return startdateXpath;
    }
    public CalendarSourceXmlScraper startdateXpath(String v) {
        this.startdateXpath = v;
        return this;
    }

    @NotNull
    private String enddateXpath = "";
    static public final String ENDDATEXPATH_PROPERTYID = "enddateXpath";
    public String enddateXpath() {
        return enddateXpath;
    }
    public CalendarSourceXmlScraper enddateXpath(String v) {
        this.enddateXpath = v;
        return this;
    }

    @NotNull
    private String starttimeXpath = "";
    static public final String STARTTIMEXPATH_PROPERTYID = "starttimeXpath";
    public String starttimeXpath() {
        return starttimeXpath;
    }
    public CalendarSourceXmlScraper starttimeXpath(String v) {
        this.starttimeXpath = v;
        return this;
    }

    @NotNull
    private String endtimeXpath = "";
    static public final String ENDTIMEXPATH_PROPERTYID = "endtimeXpath";
    public String endtimeXpath() {
        return endtimeXpath;
    }
    public CalendarSourceXmlScraper endtimeXpath(String v) {
        this.endtimeXpath = v;
        return this;
    }

    @NotNull
    private String subjectXpath = "";
    static public final String SUBJECTXPATH_PROPERTYID = "subjectXpath";
    public String subjectXpath() {
        return subjectXpath;
    }
    public CalendarSourceXmlScraper subjectXpath(String v) {
        this.subjectXpath = v;
        return this;
    }

    @NotNull
    private String datePattern;
    static public final String DATEPATTERN_PROPERTYID = "datePattern";
    public String datePattern() {
        return datePattern;
    }
    public CalendarSourceXmlScraper datePattern(String v) {
        this.datePattern = v;
        return this;
    }

    @NotNull
    private String shortMonthNotation;
    static public final String SHORTMONTHNOTATION_PROPERTYID = "shortMonthNotation";
    public String shortMonthNotation() {
        return shortMonthNotation;
    }
    public CalendarSourceXmlScraper shortMonthNotation(String v) {
        this.shortMonthNotation = v;
        return this;
    }

    @NotNull
    protected boolean nearestYear = false;
    static public final String NEARESTYEAR_PROPERTYID = "nearestYear";
    public boolean nearestYear() {
        return nearestYear;
    }
    public CalendarSourceXmlScraper nearestYear(boolean v) {
        this.nearestYear = v;
        return this;
    }

    private String startTimeDefault;
    static public final String STARTTIMEDEFAULT_PROPERTYID = "startTimeDefault";
    public String startTimeDefault() {
        return startTimeDefault;
    }
    public CalendarSourceXmlScraper startTimeDefault(String v) {
        this.startTimeDefault = v;
        return this;
    }

    private String endTimeDefault;
    static public final String ENDTIMEDEFAULT_PROPERTYID = "endTimeDefault";
    public String endTimeDefault() {
        return endTimeDefault;
    }
    public CalendarSourceXmlScraper endTimeDefault(String v) {
        this.endTimeDefault = v;
        return this;
    }

    @NotNull
    private String timePattern;
    static public final String TIMEPATTERN_PROPERTYID = "timePattern";
    public String timePattern() {
        return timePattern;
    }
    public CalendarSourceXmlScraper timePattern(String v) {
        this.timePattern = v;
        return this;
    }

    @NotNull
    private String dateTimeLocale;
    static public final String DATETIMELOCALE_PROPERTYID = "dateTimeLocale";
    public String dateTimeLocale() {
        return dateTimeLocale;
    }
    public CalendarSourceXmlScraper dateTimeLocale(String v) {
        this.dateTimeLocale = v;
        return this;
    }

    @Override
    public List<CalendarEvent> generateEvents(StringBuilder stringBuilder) {
        try {
            super.generateEvents(stringBuilder);
            if (!isEnabled()) {
                return calendarEvents;
            }

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

            // Json to XML conversion?
            if (jsonToXml) {
                JSONObject json = new JSONObject(content);
                content = XML.toString(json, "root");
                if (stringBuilder != null) stringBuilder.append("XML ").append(content).append("\n");
            }

            // Apply xpath (using saxon api because Java's API defaults to XPath 1.0)
            if (stringBuilder != null) stringBuilder.append(xpath).append("\n");
            Processor processor = new Processor(false);
            XdmNode rootXdmNode = processor.newDocumentBuilder().build(new StreamSource(new StringReader(content)));
            XPathCompiler xPathCompiler = processor.newXPathCompiler();
            XdmValue xdmValue = xPathCompiler.evaluate(xpath, rootXdmNode);
            for (XdmItem eventXdmItem : xdmValue) {
                if (stringBuilder != null) stringBuilder.append("---\nEvent node: ").append(describe(eventXdmItem)).append("\n");

                // get strings
                String startDateString = solveXpath("startdate", eventXdmItem, startdateXpath, xPathCompiler, stringBuilder);
                String endDateString = solveXpath("enddate", eventXdmItem, enddateXpath, xPathCompiler, stringBuilder);
                String starttimeString = starttimeXpath.isBlank() ? startTimeDefault : solveXpath("starttime", eventXdmItem, starttimeXpath, xPathCompiler, stringBuilder);
                String endtimeString = endtimeXpath.isBlank() ? endTimeDefault : solveXpath("endtime", eventXdmItem, endtimeXpath, xPathCompiler, stringBuilder);
                String subject = subjectXpath.isBlank() ? "" : solveXpath("subject", eventXdmItem, subjectXpath, xPathCompiler, stringBuilder);

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

                    LocalTime startLocalTime = parseLocalTime(starttimeString, timeFormatter, stringBuilder);
                    if (startLocalTime == null) {
                        if (stringBuilder != null) stringBuilder.append("Not able to determine a starttime for ").append(starttimeString);
                        continue;
                    }

                    LocalTime endLocalTime = parseLocalTime(endtimeString, timeFormatter, stringBuilder);
                    if (endLocalTime == null) {
                        if (stringBuilder != null) stringBuilder.append("Not able to determine an endtime for ").append(endtimeString);
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

            return calendarEvents();
        }
        catch (XPathExpressionException | SaxonApiException | XPathException e) {
            throw new RuntimeException(e);
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

    private String solveXpath(String id, XdmItem basenode, String xpath, XPathCompiler xPathCompiler, StringBuilder stringBuilder) throws XPathExpressionException, SaxonApiException, XPathException {
        XdmValue xdmValue = xPathCompiler.evaluate(xpath, basenode);
        if (stringBuilder != null) stringBuilder.append(id + " node: ").append(describe(xdmValue)).append("\n");
        String str = xdmValue.getUnderlyingValue().getStringValue();
        if (stringBuilder != null) stringBuilder.append(id + " string: ").append(str).append("\n");
        return str;
    }

    private LocalTime parseLocalTime(String timeString, DateTimeFormatter timeFormatter, StringBuilder stringBuilder) {
        if (stringBuilder != null) stringBuilder.append("Parsing '").append(timeString).append("' with '").append(timePattern).append("'\n");
        LocalTime localTime = LocalTime.parse(timeString, timeFormatter);
        if (stringBuilder != null) stringBuilder.append("Parsed as ").append(localTime).append("\n");
        return localTime;
    }

    private LocalDate parseLocalDate(String dateString, DateTimeFormatter dateFormatter, StringBuilder stringBuilder) {
        LocalDate localDate;
        if (nearestYear) {
            MonthDay monthDay = MonthDay.parse(dateString, dateFormatter);
            localDate = determineDateByNearestYear(monthDay, stringBuilder);
        } else {
            if (stringBuilder != null) stringBuilder.append("Parsing '").append(dateString).append("' with '").append(datePattern).append("'\n");
            localDate = LocalDate.parse(dateString, dateFormatter);
            if (stringBuilder != null) stringBuilder.append("Parsed as ").append(localDate).append("\n");
        }
        return localDate;
    }

    private String describe(XdmItem xdmItem) {
        return describe((XdmNode)xdmItem);
    }

    private String describe(XdmValue xdmValue) {
        return describe((XdmNode)xdmValue);
    }

    private String describe(XdmNode node) {
        if (node == null) {
            return "null";
        }
        XdmNode parent = node.getParent();
        if (parent == null) {
            return ""; // do not toString the root, it will print the whole XML
        }
        return describe(parent) + (node.getNodeName() == null ? "" : "/" + node.getNodeName());
    }
}
