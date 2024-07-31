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
    static public final String JSONTOXML = "jsonToXml";
    public boolean jsonToXml() {
        return jsonToXml;
    }
    public CalendarSourceXmlScraper jsonToXml(boolean v) {
        this.jsonToXml = v;
        return this;
    }

    @NotNull
    private String xpath = "";
    static public final String XPATH = "xpath";
    public String xpath() {
        return xpath;
    }
    public CalendarSourceXmlScraper xpath(String v) {
        this.xpath = v;
        return this;
    }

    @NotNull
    private String startdateXpath = "";
    static public final String STARTDATEXPATH = "startdateXpath";
    public String startdateXpath() {
        return startdateXpath;
    }
    public CalendarSourceXmlScraper startdateXpath(String v) {
        this.startdateXpath = v;
        return this;
    }

    @NotNull
    private String enddateXpath = "";
    static public final String ENDDATEXPATH = "enddateXpath";
    public String enddateXpath() {
        return enddateXpath;
    }
    public CalendarSourceXmlScraper enddateXpath(String v) {
        this.enddateXpath = v;
        return this;
    }

    @NotNull
    private String starttimeXpath = "";
    static public final String STARTTIMEXPATH = "starttimeXpath";
    public String starttimeXpath() {
        return starttimeXpath;
    }
    public CalendarSourceXmlScraper starttimeXpath(String v) {
        this.starttimeXpath = v;
        return this;
    }

    @NotNull
    private String endtimeXpath = "";
    static public final String ENDTIMEXPATH = "endtimeXpath";
    public String endtimeXpath() {
        return endtimeXpath;
    }
    public CalendarSourceXmlScraper endtimeXpath(String v) {
        this.endtimeXpath = v;
        return this;
    }

    @NotNull
    private String subjectXpath = "";
    static public final String SUBJECTXPATH = "subjectXpath";
    public String subjectXpath() {
        return subjectXpath;
    }
    public CalendarSourceXmlScraper subjectXpath(String v) {
        this.subjectXpath = v;
        return this;
    }

    @NotNull
    private String datePattern;
    static public final String DATEPATTERN = "datePattern";
    public String datePattern() {
        return datePattern;
    }
    public CalendarSourceXmlScraper datePattern(String v) {
        this.datePattern = v;
        return this;
    }

    @NotNull
    private String shortMonthNotation;
    static public final String SHORTMONTHNOTATION = "shortMonthNotation";
    public String shortMonthNotation() {
        return shortMonthNotation;
    }
    public CalendarSourceXmlScraper shortMonthNotation(String v) {
        this.shortMonthNotation = v;
        return this;
    }

    @NotNull
    protected boolean nearestYear = false;
    static public final String NEARESTYEAR = "nearestYear";
    public boolean nearestYear() {
        return nearestYear;
    }
    public CalendarSourceXmlScraper nearestYear(boolean v) {
        this.nearestYear = v;
        return this;
    }

    private String startTimeDefault;
    static public final String STARTTIMEDEFAULT = "startTimeDefault";
    public String startTimeDefault() {
        return startTimeDefault;
    }
    public CalendarSourceXmlScraper startTimeDefault(String v) {
        this.startTimeDefault = v;
        return this;
    }

    private String endTimeDefault;
    static public final String ENDTIMEDEFAULT = "endTimeDefault";
    public String endTimeDefault() {
        return endTimeDefault;
    }
    public CalendarSourceXmlScraper endTimeDefault(String v) {
        this.endTimeDefault = v;
        return this;
    }

    @NotNull
    private String timePattern;
    static public final String TIMEPATTERN = "timePattern";
    public String timePattern() {
        return timePattern;
    }
    public CalendarSourceXmlScraper timePattern(String v) {
        this.timePattern = v;
        return this;
    }

    @NotNull
    private String dateTimeLocale;
    static public final String DATETIMELOCALE = "dateTimeLocale";
    public String dateTimeLocale() {
        return dateTimeLocale;
    }
    public CalendarSourceXmlScraper dateTimeLocale(String v) {
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
            Locale locale = new Locale(dateTimeLocale);
            logAppend("Locale " + locale + "\n");
            DateTimeFormatter dateFormatter = createDateFormatter(datePattern, shortMonthNotation, locale);
            DateTimeFormatter timeFormatter = createTimeFormatter(timePattern, locale);

            // Get contents
            String content = readScrapeUrl();
            if (content.isBlank()) {
                status("No contents");
                return List.of();
            }
            content = sanatizeContent(content);

            // Json to XML conversion?
            if (jsonToXml) {
                content = JsonToXml.ofUnformatted().convert(content);
                logAppend("XML " + content + "\n");
            }

            // Apply xpath (using saxon api because Java's API defaults to XPath 1.0)
            logAppend(xpath + "\n");
            Processor processor = new Processor(false);
            XdmNode rootXdmNode = processor.newDocumentBuilder().build(new StreamSource(new StringReader(content)));
            XPathCompiler xPathCompiler = processor.newXPathCompiler();
            XdmValue xdmValue = xPathCompiler.evaluate(xpath, rootXdmNode);
            for (XdmItem eventXdmItem : xdmValue) {
                logAppend("---\nEvent node: " + describe(eventXdmItem) + "\n");

                // get strings
                String startDateString = solveXpath("startdate", eventXdmItem, startdateXpath, xPathCompiler);
                String endDateString = solveXpath("enddate", eventXdmItem, enddateXpath, xPathCompiler);
                String starttimeString = starttimeXpath.isBlank() ? startTimeDefault : solveXpath("starttime", eventXdmItem, starttimeXpath, xPathCompiler);
                String endtimeString = endtimeXpath.isBlank() ? endTimeDefault : solveXpath("endtime", eventXdmItem, endtimeXpath, xPathCompiler);
                String subject = subjectXpath.isBlank() ? "" : solveXpath("subject", eventXdmItem, subjectXpath, xPathCompiler);

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

                    LocalTime startLocalTime = parseLocalTime(starttimeString, timeFormatter);
                    if (startLocalTime == null) {
                        logAppend("Not able to determine a starttime for " + starttimeString);
                        continue;
                    }

                    LocalTime endLocalTime = parseLocalTime(endtimeString, timeFormatter);
                    if (endLocalTime == null) {
                        logAppend("Not able to determine an endtime for " + endtimeString);
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
            logAppend("Done\n");

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
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            logAppend(stringWriter.toString());
            throw e;
        }
    }

    private String solveXpath(String id, XdmItem basenode, String xpath, XPathCompiler xPathCompiler) throws XPathExpressionException, SaxonApiException, XPathException {
        XdmValue xdmValue = xPathCompiler.evaluate(xpath, basenode);
        logAppend(id + " node: " + describe(xdmValue) + "\n");
        String str = xdmValue.getUnderlyingValue().getStringValue();
        logAppend(id + " string: " + str + "\n");
        return str;
    }

    private LocalTime parseLocalTime(String timeString, DateTimeFormatter timeFormatter) {
        logAppend("Parsing '" + timeString + "' with '" + timePattern + "'\n");
        LocalTime localTime = LocalTime.parse(timeString, timeFormatter);
        logAppend("Parsed as " + localTime + "\n");
        return localTime;
    }

    private LocalDate parseLocalDate(String dateString, DateTimeFormatter dateFormatter) {
        LocalDate localDate;
        if (nearestYear) {
            MonthDay monthDay = MonthDay.parse(dateString, dateFormatter);
            localDate = determineDateByNearestYear(monthDay);
        } else {
            logAppend("Parsing '" + dateString + "' with '" + datePattern + "'\n");
            localDate = LocalDate.parse(dateString, dateFormatter);
            logAppend("Parsed as " + localDate + "\n");
        }
        return localDate;
    }

    private String describe(XdmItem xdmItem) {
        return describe((XdmNode)xdmItem);
    }

    private String describe(XdmValue xdmValue) {
        if (xdmValue instanceof XdmNode xdmNode) {
            return describe(xdmNode);
        }
        return "" + xdmValue;
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
