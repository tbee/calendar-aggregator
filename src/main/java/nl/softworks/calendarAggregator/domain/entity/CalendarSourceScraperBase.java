package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.tbee.jakarta.validator.UrlValidator;

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

	public String toString() {
		return super.toString() //
		     + ",scrapeUrl=" + scrapeUrl
		     ;
	}
}
