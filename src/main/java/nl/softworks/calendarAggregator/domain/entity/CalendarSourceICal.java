package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.TzId;
import nl.softworks.calendarAggregator.domain.boundary.R;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.tbee.jakarta.validator.UrlValidator;

import javax.swing.text.html.HTML;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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


	@Override
	public List<CalendarEvent> generateEvents(StringBuilder stringBuilder) {
		try {
			calendarEvents.removeIf(ce -> ce.generated);
			status("");

			// Get ical as string
			String icalContent = IOUtils.toString(new URL(icalUrl));
			if (stringBuilder != null) stringBuilder.append(icalContent).append("\n");
			if (icalContent.isBlank()) {
				status("No contents");
				return List.of();
			}

			// Parse ical
			CalendarBuilder builder = new CalendarBuilder();
			Calendar calendar = builder.build(new StringReader(icalContent));

			// Loop over components and find events
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

				DtEnd endDate = vEvent.getEndDate();
				if (stringBuilder != null) stringBuilder.append("endDate = ").append(endDate);
				LocalDateTime endLocalDateTime = LocalDateTime.ofInstant(endDate.getDate().toInstant(), ZoneId.systemDefault());

				String summary = vEvent.getSummary().getValue();

				String timezoneName = startDate.getTimeZone().getVTimeZone().getTimeZoneId().getValue();
				if (!timezone().name().equals(timezoneName)) {
					throw new RuntimeException("Source's timezone is not equal to " + timezoneName);
				}

				CalendarEvent calendarEvent = new CalendarEvent()
						.subject(summary)
						.startDateTime(startLocalDateTime)
						.endDateTime(endLocalDateTime);
				addCalendarEvent(calendarEvent);
			}

			if (stringBuilder != null) stringBuilder.append("Done\n");
			if (calendarEvents().isEmpty()) {
				status("No events are generated");
				return List.of();
			}

			status("ok");
			return calendarEvents();
		}
		catch (RuntimeException | IOException | ParserException e) {
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
