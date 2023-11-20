package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.tbee.jakarta.validator.UrlValidator;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

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

	protected String readScrapeUrl() {
		try {
			Document doc = Jsoup.connect(scrapeUrl).get();
			String content = doc.text();
			if (scrapeBlockStart != null && !scrapeBlockStart.isBlank()) {
				content = content.substring(content.indexOf(scrapeBlockStart.trim()));
			}
			if (scrapeBlockEnd != null && !scrapeBlockEnd.isBlank()) {
				content = content.substring(0, content.indexOf(scrapeBlockEnd.trim()));
			}
			return content;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String toString() {
		return super.toString() //
		     + ",scrapeUrl=" + scrapeUrl
		     ;
	}
}
