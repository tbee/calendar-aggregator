package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
	static public final String ICALURL_PROPERTYID = "icalUrl";
	public String icalUrl() {
		return icalUrl;
	}
	public CalendarSourceICal icalUrl(String v) {
		this.icalUrl = v;
		return this;
	}

	@NotNull
	private String regex;
	static public final String REGEX_PROPERTYID = "regex";
	public String regex() {
		return regex;
	}
	public CalendarSourceICal regex(String v) {
		this.regex = v;
		return this;
	}

	@Override
	public List<CalendarEvent> generateEvents(StringBuilder stringBuilder) {
		try {
			calendarEvents.removeIf(ce -> ce.generated);
			status("");

			// Get ical as string
			String url = resolveUrl(icalUrl, stringBuilder);
			if (stringBuilder != null) stringBuilder.append("url = ").append(url);
			String icalContent = getUrl(url);
			if (stringBuilder != null) {
				String logContent = (icalContent.length() > 10000 ? icalContent.substring(0, 10000) + "\n...\n" : icalContent);
				stringBuilder.append(logContent).append("\n");
			}
			if (icalContent.isBlank()) {
				status("No contents");
				return List.of();
			}

			// Parse ical
			LocalDateTime pastThreshold = LocalDateTime.now().minusMonths(1);
			if (stringBuilder != null) stringBuilder.append("pastThreshold = ").append(pastThreshold);
			LocalDateTime futureThreshold = LocalDateTime.now().plusMonths(6);
			if (stringBuilder != null) stringBuilder.append("futureThreshold = ").append(futureThreshold);
			CalendarBuilder builder = new CalendarBuilder();
			Calendar calendar = builder.build(new StringReader(icalContent));

			// Loop over components and find events
			if (stringBuilder != null) stringBuilder.append("regex = ").append(regex).append("\n");
			Pattern pattern = regex == null || regex.isEmpty() ? null : Pattern.compile(regex);
			ComponentList<CalendarComponent> components = calendar.getComponents();
			if (stringBuilder != null) stringBuilder.append("#components = ").append(components.size()).append("\n");
			for (Component component : components) {
				if (!(component instanceof VEvent vEvent)) {
					continue;
				}
				if (stringBuilder != null) stringBuilder.append("---").append("\n");

				DtStart startDate = vEvent.getStartDate();
				if (stringBuilder != null) stringBuilder.append("startDate = ").append(startDate);
				LocalDateTime startLocalDateTime = LocalDateTime.ofInstant(startDate.getDate().toInstant(), ZoneId.systemDefault());
				if (startLocalDateTime.isBefore(pastThreshold) || startLocalDateTime.isAfter(futureThreshold)) {
					if (stringBuilder != null) stringBuilder.append("Outside threshold\n");
					continue;
				}

				DtEnd endDate = vEvent.getEndDate();
				if (stringBuilder != null) stringBuilder.append("endDate = ").append(endDate);
				LocalDateTime endLocalDateTime = LocalDateTime.ofInstant(endDate.getDate().toInstant(), ZoneId.systemDefault());

				String summary = vEvent.getSummary().getValue();
				if (stringBuilder != null) stringBuilder.append("summary = ").append(summary).append("\n");
				if (pattern != null) {
					Matcher matcher = pattern.matcher(summary);
					if (!matcher.matches()) {
						if (stringBuilder != null) stringBuilder.append("Does not match the regexp\n");
						continue;
					} else if (stringBuilder != null) {
						stringBuilder.append("Start index: ").append(matcher.start()).append("\n");
						stringBuilder.append("End index: ").append(matcher.end()).append("\n");
						stringBuilder.append("Matched string: ").append(summary, matcher.start(), matcher.end()).append("\n");
						for (int i = 0; i < matcher.groupCount() + 1; i++) {
							stringBuilder.append("Group ").append(i).append(" = ").append(matcher.group(i)).append("\n");
						}
					}
				}
				TimeZone timeZone = startDate.getTimeZone();
				if (timeZone != null) {
					String timezoneName = timeZone.getVTimeZone().getTimeZoneId().getValue();
					if (!timezone().name().equals(timezoneName)) {
						throw new RuntimeException("Source's timezone is not equal to " + timezoneName);
					}
				}

				CalendarEvent calendarEvent = new CalendarEvent()
						.subject(summary)
						.startDateTime(startLocalDateTime)
						.endDateTime(endLocalDateTime);
				addCalendarEvent(calendarEvent);
			}

			if (stringBuilder != null) stringBuilder.append("Done\n");
			if (calendarEvents().isEmpty()) {
				status("No events");
				return List.of();
			}

			status(OK);
			return calendarEvents();
		}
		catch (RuntimeException | IOException | ParserException | InterruptedException e) {
			status(e.getMessage());
			if (stringBuilder != null) {
				StringWriter stringWriter = new StringWriter();
				e.printStackTrace(new PrintWriter(stringWriter));
				stringBuilder.append(stringWriter);
			}
			throw new RuntimeException(e);
		}
	}

	public String toString() {
		return super.toString() //
		     + ",icalUrl=" + icalUrl
		     ;
	}
}
