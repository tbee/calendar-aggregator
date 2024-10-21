package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.MappedSuperclass;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@MappedSuperclass
abstract public class CalendarSourceScraperBase extends CalendarSource {

	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarSourceScraperBase.class);

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

	protected String scrapeUrl;
	static public final String SCRAPEURL = "scrapeUrl";
	public String scrapeUrl() {
		return scrapeUrl;
	}
	public CalendarSourceScraperBase scrapeUrl(String v) {
		this.scrapeUrl = v;
		return this;
	}
	@AssertTrue(message = "Scraper URL is not a valid URL")
	public boolean isValidScraperURL() {
		return scrapeUrl == null || scrapeUrl.isBlank() || UrlValidatorImpl.isValid(resolveUrl(scrapeUrl));
	}

	protected String readScrapeUrl() {
		try {
			String url = resolveUrl(scrapeUrl);
			logAppend("Reading: " + url + "\n");
            return getUrl(url);
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	protected DateTimeFormatter createDateFormatter(String datePattern, String shortMonthNotation, Locale locale) {
		logAppend("datePattern " + datePattern + "\n");
		if (DEFAULT_FORMATTERS.containsKey(datePattern)) {
			return DEFAULT_FORMATTERS.get(datePattern);
		}

		// Default setting
		DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder().parseCaseInsensitive();

		// either use the custom short month notation, or just a regular pattern
		if (datePattern.contains(SHORT_MONTH_NOTATION_PATTERN)) {
			int idx = datePattern.indexOf(SHORT_MONTH_NOTATION_PATTERN);
			if (idx > 0) {
				dateTimeFormatterBuilder.appendPattern(datePattern.substring(0, idx));
			}
			dateTimeFormatterBuilder.appendText(ChronoField.MONTH_OF_YEAR, constructShortMonthsLookup(shortMonthNotation));
			if (idx + SHORT_MONTH_NOTATION_PATTERN.length() < datePattern.length()) {
				dateTimeFormatterBuilder.appendPattern(datePattern.substring(idx + SHORT_MONTH_NOTATION_PATTERN.length()));
			}
		}
		else {
			dateTimeFormatterBuilder.appendPattern(datePattern);
		}
        return dateTimeFormatterBuilder.toFormatter(locale);
	}

	private static Map<Long, String> constructShortMonthsLookup(String shortMonthNotation) {
		Map<Long, String> lookup = new HashMap<>();
		String[] shortMonths = shortMonthNotation.split("\\|");
		if (shortMonths.length != 12) {
			throw new RuntimeException("Short months do not contain 12 entries, use the | symbol as a separator");
		}
		for (int i = 0; i < shortMonths.length; i++) {
			lookup.put((long)(i + 1), shortMonths[i]);
		}
		return lookup;
	}

	protected DateTimeFormatter createTimeFormatter(String timePattern, Locale locale) {
		logAppend("timePattern " + timePattern + "\n");
		if (DEFAULT_FORMATTERS.containsKey(timePattern)) {
			return DEFAULT_FORMATTERS.get(timePattern);
		}
        return new DateTimeFormatterBuilder()
				.parseCaseInsensitive()
				.appendPattern(timePattern)
				.toFormatter(locale);
	}

	protected LocalDate determineDateByNearestYear(MonthDay monthDay) {
		LocalDate now = LocalDate.now();

		// Calculate the three options
		int year = now.getYear();
		LocalDate lastYearsDate = toLocalDate(year - 1, monthDay.getMonth(), monthDay.getDayOfMonth());
		LocalDate thisYearsDate = toLocalDate(year, monthDay.getMonth(), monthDay.getDayOfMonth());
		LocalDate nextYearsDate = toLocalDate(year + 1, monthDay.getMonth(), monthDay.getDayOfMonth());

		// Determine the distance (period) from now
		int lastYearsPeriod = lastYearsDate == null ? Integer.MAX_VALUE : asDays(abs(Period.between(now, lastYearsDate)));
		int thisYearsPeriod = thisYearsDate == null ? Integer.MAX_VALUE : asDays(abs(Period.between(now, thisYearsDate)));
		int nextYearsPeriod = nextYearsDate == null ? Integer.MAX_VALUE : asDays(abs(Period.between(now, nextYearsDate)));

		// Select the nearest
		int bestPeriod = lastYearsPeriod;
		LocalDate bestDate = lastYearsDate;
		if (thisYearsPeriod < bestPeriod) {
			bestPeriod = thisYearsPeriod;
			bestDate = thisYearsDate;
		}
		if (nextYearsPeriod < bestPeriod) {
			//bestPeriod = nextYearsPeriod;
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

	private LocalDate toLocalDate(int year, Month month, int dayOfMonth) {
		try {
			return LocalDate.of(year, month, dayOfMonth);
		}
		catch (DateTimeException e) {
			logAppend("For " + year + "-" + month.getValue() + "-" + dayOfMonth + ": " + e.getMessage());
//			else LOGGER.warn("Problems creating a LocalDate " + year + "-" + month.getValue() + "-" + dayOfMonth, e);
			return null;
		}
	}

	public String toString() {
		return super.toString() //
		     + ",scrapeUrl=" + scrapeUrl
		     ;
	}
}
