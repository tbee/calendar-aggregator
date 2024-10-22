package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.HtmlUtils;
import org.tbee.jakarta.validator.UrlValidatorImpl;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@MappedSuperclass
abstract public class CalendarSourceScraperBaseHTML extends CalendarSourceScraperBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarSourceScraperBaseHTML.class);

	final public static String SHORT_MONTH_NOTATION_PATTERN = "SMN";
	final public static Map<String, DateTimeFormatter> DEFAULT_FORMATTERS = Map.of(
			"ISO_ZONED_DATE_TIME", DateTimeFormatter.ISO_ZONED_DATE_TIME,
			"ISO_OFFSET_DATE_TIME", DateTimeFormatter.ISO_OFFSET_DATE_TIME,
			"ISO_OFFSET_DATE", DateTimeFormatter.ISO_OFFSET_DATE,
			"ISO_OFFSET_TIME", DateTimeFormatter.ISO_OFFSET_TIME,
			"ISO_LOCAL_DATE_TIME", DateTimeFormatter.ISO_LOCAL_DATE_TIME,
			"ISO_LOCAL_DATE", DateTimeFormatter.ISO_LOCAL_DATE,
			"ISO_LOCAL_TIME", DateTimeFormatter.ISO_LOCAL_TIME,
			"ISO_DATE_TIME", DateTimeFormatter.ISO_DATE_TIME,
			"ISO_DATE", DateTimeFormatter.ISO_DATE,
			"ISO_TIME", DateTimeFormatter.ISO_TIME
		);

	protected String scrapeBlockStart;
	static public final String SCRAPEBLOCKSTART = "scrapeBlockStart";
	public String scrapeBlockStart() {
		return scrapeBlockStart;
	}
	public CalendarSourceScraperBaseHTML scrapeBlockStart(String v) {
		this.scrapeBlockStart = v;
		return this;
	}

	protected String scrapeBlockEnd;
	static public final String SCRAPEBLOCKEND = "scrapeBlockEnd";
	public String scrapeBlockEnd() {
		return scrapeBlockEnd;
	}
	public CalendarSourceScraperBaseHTML scrapeBlockEnd(String v) {
		this.scrapeBlockEnd = v;
		return this;
	}

	@NotNull
	private String removeChars = "";
	static public final String CONTENT = "removeChars";
	public String removeChars() {
		return removeChars;
	}
	public CalendarSourceScraperBaseHTML removeChars(String v) {
		this.removeChars = v;
		return this;
	}

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "calendarSource", fetch = FetchType.EAGER)
	protected final List<CalendarSourcePreprocess> calendarSourcePreprocesses = new ArrayList<>();
	public List<CalendarSourcePreprocess> calendarSourcePreprocesses() {
		return Collections.unmodifiableList(calendarSourcePreprocesses);
	}
	public void calendarSourcePreprocesses(Collection<CalendarSourcePreprocess> v) {
		// TODO this can be done more efficient
		calendarSourcePreprocesses.forEach(la -> la.calendarSource = null); // delete
		calendarSourcePreprocesses.clear();
		v.forEach(la -> la.calendarSource = this);
		calendarSourcePreprocesses.addAll(v);
	}
	public void addLabelAssignment(CalendarSourcePreprocess v) {
		calendarSourcePreprocesses.add(v);
	}
	public void removeLabelAssignment(CalendarSourcePreprocess v) {
		calendarSourcePreprocesses.remove(v);
	}

	protected String readScrapeUrlHTML() {
		String html = readScrapeUrl();

		// Preprocess
		for (CalendarSourcePreprocess calendarSourcePreprocess : calendarSourcePreprocesses) {
			html = calendarSourcePreprocess.preprocess(html);
		}
		//if (!calendarSourcePreprocesses.isEmpty()) logAppend("Preprocessed: " + html + "\n");

		// Extract text information
		Document doc = Jsoup.parse(html);
		String text = doc.text();
		logAppend("Content: " + text.length() + "\n");

		// special handling for certain elements: <eventbrite-modal :events="html escaped string"
		for (Element eventbrightModalElement : doc.selectXpath("//eventbrite-modal")) {
			Attribute eventsAttribute = eventbrightModalElement.attribute(":events");
			if (eventsAttribute != null) {
				text = "{\"event\":" + HtmlUtils.htmlUnescape(eventsAttribute.getValue()) + "}";
				logAppend("Content, added eventbrite-modal\n");
			}
		}

		// extract block
		if (scrapeBlockStart != null && !scrapeBlockStart.isBlank()) {
			text = text.substring(text.indexOf(scrapeBlockStart));
			logAppend("Content after block start: " + text.length() + "\n");
		}
		if (scrapeBlockEnd != null && !scrapeBlockEnd.isBlank()) {
			text = text.substring(0, text.indexOf(scrapeBlockEnd));
			logAppend("Content after block end: " + text.length() + "\n");
		}
		return text;
	}

	protected String sanatizeContent(String content) {
		content = content.replace("\n", " ");
		logAppend("Removing characters: "+ removeChars + "\n");
		for (int i = 0; i < removeChars.length(); i++) {
			String removeChar = removeChars.substring(i, i+1);
			content = content.replace(removeChar, " ");
		}
		while (content.contains("  ")) {
			content = content.replace("  ", " ");
		}
		logAppend(content + "\n---\n");
		return content;
	}

	public String toString() {
		return super.toString() //
		     ;
	}
}
