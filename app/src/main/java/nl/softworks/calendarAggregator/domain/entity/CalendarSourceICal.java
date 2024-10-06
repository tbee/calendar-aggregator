package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import nl.softworks.calendarAggregator.domain.service.ICalService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@DiscriminatorValue("ical")
public class CalendarSourceICal extends CalendarSource {

	public String type() {
		return "ICal";
	}

	@NotNull
	@Column(name = "scrapeUrl")
	protected String icalUrl;
	static public final String ICALURL = "icalUrl";
	public String icalUrl() {
		return icalUrl;
	}
	public CalendarSourceICal icalUrl(String v) {
		this.icalUrl = v;
		return this;
	}

	@NotNull
	private String regex;
	static public final String REGEX = "regex";
	public String regex() {
		return regex;
	}
	public CalendarSourceICal regex(String v) {
		this.regex = v;
		return this;
	}

	@Override
	public List<CalendarEvent> generateEvents() {
		try {
			super.generateEvents();
			if (!isEnabled()) {
				return calendarEvents;
			}

			// Get ical as string
			String url = resolveUrl(icalUrl);
			logAppend("url = " + url);
			String icalContent = new ICalService().sanatize(getUrl(url));
			String logContent = (icalContent.length() > 10000 ? icalContent.substring(0, 10000) + "\n...\n" : icalContent);
			logAppend(logContent + "\n");
			if (icalContent.isBlank()) {
				status("No contents");
				return List.of();
			}

			// Parse ical
			LocalDateTime pastThreshold = LocalDateTime.now().minusMonths(1);
			logAppend("pastThreshold = " + pastThreshold);
			LocalDateTime futureThreshold = LocalDateTime.now().plusMonths(6);
			logAppend("futureThreshold = " + futureThreshold);
			CalendarBuilder builder = new CalendarBuilder();
			Calendar calendar = builder.build(new StringReader(icalContent));

			// Prepare regex for summary
			logAppend("regex = " + regex + "\n");
			Pattern pattern = regex == null || regex.isEmpty() ? null : Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

			// Loop over components and find events
			List<CalendarComponent> components = calendar.getComponents();
			logAppend("#components = " + components.size() + "\n");
			for (Component component : components) {
				if (!(component instanceof VEvent vEvent)) {
					continue;
				}
				logAppend("---" + "\n");

				// Validate startDate
				DtStart<Temporal> startDate = vEvent.getStartDate().orElseThrow();
				logAppend("startDate = " + startDate);
				LocalDateTime startLocalDateTime = toLocalDateTime(startDate);
				if (startLocalDateTime.isBefore(pastThreshold) || startLocalDateTime.isAfter(futureThreshold)) {
					logAppend("Outside threshold\n");
					continue;
				}

				// Get endDate
				DtEnd endDate = vEvent.getEndDate().orElseThrow();
				logAppend("endDate = " + endDate);
				LocalDateTime endLocalDateTime = toLocalDateTime(endDate);

				// Validate summary
				String summary = vEvent.getSummary().orElseThrow().getValue();
				logAppend("summary = " + summary + "\n");
				if (pattern != null) {
					Matcher matcher = pattern.matcher(summary);
					if (!matcher.matches()) {
						logAppend("Does not match the regexp\n");
						continue;
					}
					logMatcher(matcher, summary);
				}

//				// Determine timezone
//				ZoneId timeZone = ZoneId.from(startDate.getDate());
//				if (timeZone != null) {
//					String timezoneName = timeZone.getId();
////					if (!timezone().name().equals(timezoneName)) {
////						throw new RuntimeException("Source's timezone is not equal to " + timezoneName);
////					}
//				}

				// Create event
				CalendarEvent calendarEvent = new CalendarEvent()
						.subject(summary)
						.startDateTime(startLocalDateTime)
						.endDateTime(endLocalDateTime);
				addCalendarEvent(calendarEvent);
			}

			dropExpiredEvents();
			sanatizeEvents();

			logAppend("Done\n");
			if (calendarEvents().isEmpty()) {
				status("No events");
				return List.of();
			}

			return calendarEvents();
		}
		catch (RuntimeException | IOException | ParserException | InterruptedException e) {
			status(e.getMessage());
			StringWriter stringWriter = new StringWriter();
			e.printStackTrace(new PrintWriter(stringWriter));
			logAppend(stringWriter.toString());
			throw new RuntimeException(e);
		}
	}

	private LocalDateTime toLocalDateTime(DateProperty<?> dateProperty) {
		if ("DATE".equals(dateProperty.getParameter("VALUE").orElse(new Value("")).getValue())) {
			return LocalDate.from(dateProperty.getDate()).atStartOfDay();
		}
		return LocalDateTime.from(dateProperty.getDate());
	}

	public String toString() {
		return super.toString() //
		     + ",icalUrl=" + icalUrl
		     ;
	}
}
