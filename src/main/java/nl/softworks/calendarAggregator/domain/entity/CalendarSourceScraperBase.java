package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.tbee.jakarta.validator.UrlValidator;

import java.io.IOException;
import java.net.URL;

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
	private String removeChars = "";
	static public final String CONTENT_PROPERTYID = "removeChars";
	public String removeChars() {
		return removeChars;
	}
	public CalendarSourceScraperBase removeChars(String v) {
		this.removeChars = v;
		return this;
	}


	protected String readScrapeUrl(StringBuilder stringBuilder) {
		try {
			if (stringBuilder != null) stringBuilder.append("Reading: " + scrapeUrl + "\n");
			String html = IOUtils.toString(new URL(scrapeUrl));
			Document doc = Jsoup.parse(html);
			String text = doc.text();
			if (stringBuilder != null) stringBuilder.append("Content: " + text.length() + "\n");
			if (scrapeBlockStart != null && !scrapeBlockStart.isBlank()) {
				text = text.substring(text.indexOf(scrapeBlockStart.trim()));
				if (stringBuilder != null) stringBuilder.append("Content after block start: " + text.length() + "\n");
			}
			if (scrapeBlockEnd != null && !scrapeBlockEnd.isBlank()) {
				text = text.substring(0, text.indexOf(scrapeBlockEnd.trim()));
				if (stringBuilder != null) stringBuilder.append("Content after block end: " + text.length() + "\n");
			}
			return text;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected String sanatize(String content, StringBuilder stringBuilder) {
		content = content.replace("\n", " ");
		if (stringBuilder != null) stringBuilder.append("Removing characters: ").append(removeChars).append("\n");
		for (int i = 0; i < removeChars.length(); i++) {
			String removeChar = removeChars.substring(i, i+1);
			content = content.replace(removeChar, "");
		}
		while (content.contains("  ")) {
			content = content.replace("  ", " ");
		}
		if (stringBuilder != null) stringBuilder.append(content).append("\n---\n");
		return content;
	}

	public String toString() {
		return super.toString() //
		     + ",scrapeUrl=" + scrapeUrl
		     ;
	}
}
