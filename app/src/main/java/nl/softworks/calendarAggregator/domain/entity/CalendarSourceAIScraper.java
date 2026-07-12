package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import nl.softworks.calendarAggregator.domain.boundary.S;
import org.hibernate.boot.models.xml.spi.XmlDocument;
import org.springframework.ai.chat.client.ChatClient;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@DiscriminatorValue("regex")
public class CalendarSourceAIScraper extends CalendarSourceScraperBaseHTML {

    public String type() {
        return "AI";
    }

    @NotNull
    @Column(name = "regex")
    private String prompt;
    static public final String PROMPT = "prompt";
    public String prompt() {
        return prompt;
    }
    public CalendarSourceAIScraper prompt(String v) {
        this.prompt = v;
        return this;
    }

//    public Address parseAddress(String address, String model, int timeoutInSeconds) {
//        String systemPrompt = """
//                    You are a parcel delivery service.
//                    You need to split up free form addresses into its parts.
//                    The addresses most likely are in the european union.
//                    The result must be returned as xml using the following tags: name, street, housenumber, extension, zipcode, city and country.
//                    The toplevel tag should be address.
//                    Do not add extra tags.
//                    The zipcode may contain spaces.
//                    Use the ISO code for the country.
//                    The tags are not optional, populate with an empty string.
//                    Only state the XML, nothing more.
//                    """;
//
//        String userPrompt = String.format("Split up '%s'", address.replace("\n", ""));
//
//        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
//                .addSystemMessage(systemPrompt)
//                .addUserMessage(userPrompt)
//                .model(model) // https://openrouter.ai/models
//                .build();
//        ChatCompletion chatCompletion = client.chat().completions().create(params);
//
//        Optional<String> content = chatCompletion.choices().stream()
//                .findFirst()
//                .flatMap(choice -> choice.message().content());
//
//        if (content.isEmpty()) {
//            return new Address("", "", "", "", "", "", "");
//        }
//
//        String xml = content.get().trim();
//        System.out.println(userPrompt + "\n\n" + xml);
//        if (xml.startsWith("```xml")) {
//            xml = xml.substring(6, xml.length() - 3).trim(); // Remove ```xml and ```
//        } else if (xml.startsWith("```")) {
//            xml = xml.substring(3, xml.length() - 3).trim();
//        }
//
//        XmlDocument xmlDocument = new XmlDocument(xml);
//        String name = xmlDocument.xpathString("//name");
//        String street = xmlDocument.xpathString("//street");
//        String housenumber = xmlDocument.xpathString("//housenumber");
//        String extension = xmlDocument.xpathString("//extension");
//        String zipcode = xmlDocument.xpathString("//zipcode");
//        String city = xmlDocument.xpathString("//city");
//        String country = xmlDocument.xpathString("//country");
//        return new Address(name, street, housenumber, extension, zipcode, city, country);
//    }
//
//    public XmlDocument(String xml) {
//        try {
//            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder builder = factory.newDocumentBuilder();
//            doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
//            xPath = XPathFactory.newInstance().newXPath();
//        }
//        catch (ParserConfigurationException | SAXException | IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public String xpathString(String expr) {
//        try {
//            Node node = (Node) xPath.evaluate(expr, doc, XPathConstants.NODE);
//            if (node == null) {
//                return null;
//            }
//            return node.getTextContent().trim();
//        }
//        catch (XPathExpressionException e) {
//            throw new RuntimeException(e);
//        }
//    }

    @Override
    public Set<CalendarEvent> generateEvents() {
        try {
            super.generateEvents();
            if (!isEnabled()) {
                return calendarEvents;
            }

            // Get contents
            String content = readScrapeUrlHTML();
            if (content.isBlank()) {
                status("No contents");
                return Set.of();
            }
            content = sanatizeContent(content);

            // Apply prompt
            String systemPrompt = """
                    You are a web scraper, your task is to extract dancing events from websites.
                    You need to find start and end moments for the events and return them as a XML structure similar to this:
                    <events>
                        <event>
                            <startDate>2024-01-01</startDate>
                            <endDate>2024-01-02</endDate>
                            <startTime>10:00</startTime>
                            <endTime>12:00</endTime>
                            <description>Dancing is fun</description>
                        </event>
                    </events>
                    Dates must be in the format yyyy-MM-dd.
                    Times muist be in the format HH:mm.
                    Only return the XML structure, nothing else.
                    """;
            logAppend(systemPrompt + "\n" + prompt + "\n");

            String response = ChatClient
                    .create(S.chatModel())
                    .prompt()
                    .system(systemPrompt)
                    .user(prompt)
                    .call()
                    .content();
            logAppend(response + "\n");

            dropExpiredEvents();
            sanatizeEvents();
            logAppend("Done\n");

            // set status
            if (calendarEvents().isEmpty()) {
                status("No events");
                return Set.of();
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
