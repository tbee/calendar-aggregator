package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.tbee.jakarta.validator.UrlValidator;

import java.io.IOException;

@MappedSuperclass
abstract public class CalendarSourceScraperBase extends CalendarSource {

	@UrlValidator
	protected String scrapeUrl;
	static public final String SCRAPEURL_PROPERTYID = "scrapeUrl";
	public String scrapeUrl() {
		return scrapeUrl;
	}
	public CalendarSourceScraperBase scrapeUrl(String v) {
		this.scrapeUrl = v;
		return this;
	}

	protected String scrapeBlockStart;
	static public final String SCRAPEBLOCKSTART_PROPERTYID = "scrapeBlockStart";
	public String scrapeBlockStart() {
		return scrapeBlockStart;
	}
	public CalendarSourceScraperBase scrapeBlockStart(String v) {
		this.scrapeBlockStart = v;
		return this;
	}

	protected String scrapeBlockEnd;
	static public final String SCRAPEBLOCKEND_PROPERTYID = "scrapeBlockEnd";
	public String scrapeBlockEnd() {
		return scrapeBlockEnd;
	}
	public CalendarSourceScraperBase scrapeBlockEnd(String v) {
		this.scrapeBlockEnd = v;
		return this;
	}

	@NotNull
	private String content = "";
	static public final String CONTENT_PROPERTYID = "content";
	public String content() {
		return content;
	}
	public CalendarSourceScraperBase content(String v) {
		this.content = v;
		return this;
	}


	protected String readScrapeUrl(StringBuilder stringBuilder) {
		try {
			if (stringBuilder != null) stringBuilder.append("Reading: " + scrapeUrl + "\n");
			Document doc = Jsoup.connect(scrapeUrl).get();
			String content = doc.text();
			if (stringBuilder != null) stringBuilder.append("Content: " + content.length() + "\n");
			if (scrapeBlockStart != null && !scrapeBlockStart.isBlank()) {
				content = content.substring(content.indexOf(scrapeBlockStart.trim()));
				if (stringBuilder != null) stringBuilder.append("Content after block start: " + content.length() + "\n");
			}
			if (scrapeBlockEnd != null && !scrapeBlockEnd.isBlank()) {
				content = content.substring(0, content.indexOf(scrapeBlockEnd.trim()));
				if (stringBuilder != null) stringBuilder.append("Content after block end: " + content.length() + "\n");
			}
			return content;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected String sanatize(String content, StringBuilder stringBuilder) {
		content = content.replace("\n", " ");
		if (stringBuilder != null) stringBuilder.append(content).append("\n---\n");
		return content;
	}

	public String toString() {
		return super.toString() //
		     + ",scrapeUrl=" + scrapeUrl
		     ;
	}
}
