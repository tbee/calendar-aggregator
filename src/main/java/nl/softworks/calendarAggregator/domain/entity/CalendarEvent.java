package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.parameter.Value;

import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Entity
public class CalendarEvent extends EntityBase<CalendarEvent> {

	public CalendarEvent() {
	}

	private CalendarEvent(CalendarSource calendarSource) {
		this.calendarSource = calendarSource;
	}

	@ManyToOne
	@NotNull
    CalendarSource calendarSource;
	public CalendarSource calendarSource() {
		return calendarSource;
	}

	@NotNull
	private LocalDateTime startDateTime;
	static public final String STARTDATETIME_PROPERTYID = "startDateTime";
	public LocalDateTime startDateTime() {
		return startDateTime;
	}
	public CalendarEvent startDateTime(LocalDateTime v) {
		this.startDateTime = v;
		return this;
	}

	@NotNull
	private LocalDateTime endDateTime;
	static public final String ENDDATETIME_PROPERTYID = "endDateTime";
	public LocalDateTime endDateTime() {
		return endDateTime;
	}
	public CalendarEvent endDateTime(LocalDateTime v) {
		this.endDateTime = v;
		return this;
	}

	@NotNull
	private String subject = "";
	static public final String SUBJECT_PROPERTYID = "subject";
	public String subject() {
		return subject;
	}

	public CalendarEvent subject(String v) {
		this.subject = v;
		return this;
	}

	@NotNull
	protected boolean generated = true;
	static public final String GENERATED_PROPERTYID = "generated";
	public boolean generated() {
		return generated;
	}
	public CalendarEvent generated(boolean v) {
		this.generated = v;
		return this;
	}

	@NotNull
	private String rrule = "";
	static public final String RRULE_PROPERTYID = "rrule";
	public String rrule() {
		return rrule;
	}

	public CalendarEvent rrule(String v) {
		this.rrule = v;
		return this;
	}

	public List<CalendarEvent> applyRRule() {
		if (rrule.isBlank()) {
			return List.of(this);
		}

		// Duration is needed to calculate end from start
		Duration duration = Duration.between(startDateTime, endDateTime);
		try {
			// Limit the amount of events generated
			LocalDateTime pastTreshold = LocalDateTime.now().minusMonths(1);
			LocalDateTime futureTreshold = LocalDateTime.now().plusMonths(5);

			// Create the recurrence rule
			Recur recur = new Recur(rrule);
			DateTime recurStartDateTime = toDateTime(startDateTime);
			Date recurEndDateTime = recur.getUntil();
			if (recurEndDateTime == null) {
				recurEndDateTime = toDateTime(futureTreshold);
			}
			DateList dateList = recur.getDates(recurStartDateTime, recurEndDateTime, Value.DATE_TIME);

			// Convert dates to events
			List<CalendarEvent> calendarEvents = dateList.stream()
					.map(d -> new CalendarEvent(CalendarEvent.this.calendarSource)
							.startDateTime(toLocalDateTime(d))
							.endDateTime(toLocalDateTime(d).plus(duration))
							.subject(CalendarEvent.this.subject))
					.filter(ce -> ce.startDateTime.isAfter(pastTreshold))
					.filter(ce -> ce.startDateTime.isBefore(futureTreshold))
					.toList();
			return calendarEvents;
		}
		catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private Date toDate(LocalDateTime localDateTime) {
		return new Date(java.sql.Timestamp.valueOf(localDateTime));
	}
	private DateTime toDateTime(LocalDateTime localDateTime) {
		return new DateTime(java.sql.Timestamp.valueOf(localDateTime));
	}
	private LocalDateTime toLocalDateTime(Date date) {
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	@Override
	public String toString() {
		return super.toString() //
			+ ",startDateTime=" + startDateTime
			+ ",endDateTime=" + endDateTime
			+ ",rrule=" + rrule
		    ;
	}
}
