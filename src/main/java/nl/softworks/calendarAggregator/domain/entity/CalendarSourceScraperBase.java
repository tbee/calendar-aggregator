package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.tbee.jakarta.validator.UrlValidator;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Period;

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

	protected LocalDate determineDateByNearestYear(MonthDay monthDay) {
		LocalDate now = LocalDate.now();

		int year = now.getYear();
		LocalDate lastYearsDate = LocalDate.of(year - 1, monthDay.getMonth(), monthDay.getDayOfMonth());
		LocalDate thisYearsDate = LocalDate.of(year, monthDay.getMonth(), monthDay.getDayOfMonth());
		LocalDate nextYearsDate = LocalDate.of(year + 1, monthDay.getMonth(), monthDay.getDayOfMonth());

		int lastYearsPeriod = asDays(abs(Period.between(now, lastYearsDate)));
		int thisYearsPeriod = asDays(abs(Period.between(now, thisYearsDate)));
		int nextYearsPeriod = asDays(abs(Period.between(now, nextYearsDate)));

		int bestPeriod = lastYearsPeriod;
		LocalDate bestDate = lastYearsDate;
		if (thisYearsPeriod < bestPeriod) {
			bestPeriod = thisYearsPeriod;
			bestDate = thisYearsDate;
		}
		if (nextYearsPeriod < bestPeriod) {
			bestPeriod = nextYearsPeriod;
			bestDate = nextYearsDate;
		}
		return bestDate;
	}
	private Period abs(Period period) {
		return (period.isNegative() ? period.negated() : period);
	}
	private int asDays(Period p) {
		if (p == null) {
			return 0;
		}
		return (p.getYears() * 12 + p.getMonths()) * 30 + p.getDays();
	}

	public String toString() {
		return super.toString() //
		     + ",scrapeUrl=" + scrapeUrl
		     ;
	}
}
