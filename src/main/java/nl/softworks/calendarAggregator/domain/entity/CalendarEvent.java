package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
public class CalendarEvent extends EntityBase<CalendarEvent> {

	@ManyToOne
	@NotNull
    CalendarSource calendarSource;

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

	private String subject;
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


	public String ical() {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

		// https://www.kanzaki.com/docs/ical/location.html
		return 	"""
				BEGIN:VEVENT
				UID:%uid%
				DTSTAMP:%dtStart%
				DTSTART;TZID=%tzid%:%dtStart%
				DTEND;TZID=%tzid%:%dtEnd%
				TRANSP:OPAQUE
				CLASS:PUBLIC
				SUMMARY:%summary%
				DESCRIPTION:%description%
				LOCATION:%location%
				END:VEVENT
				"""
				.replace("%uid%", id() + "@calendarAggregator.tbee.org")
				.replace("%tzid%", calendarSource.timezone().name())
				.replace("%dtStart%", dateTimeFormatter.format(startDateTime))
				.replace("%dtEnd%", dateTimeFormatter.format(endDateTime))
				.replace("%summary%", (calendarSource.name() + " " + subject).trim())
				.replace("%location%", calendarSource.location().replace("\n", ", "))
				.replace("%description%", calendarSource.url())
				;
/*

		BEGIN:VEVENT
		DTSTAMP:20231111T091847
		UID:2023-02-26A@selfroster.softworks.nl
				DTSTART;TZID=Europe/Amsterdam:20230226T190000
				DTEND;TZID=Europe/Amsterdam:20230226T230000
		SEQUENCE:0
		TRANSP:OPAQUE
		CLASS:PUBLIC
		SUMMARY:Avond
		DESCRIPTION:
		END:VEVENT
*/
	}

	@Override
	public String toString() {
		return super.toString() //
			+ ",startDateTime=" + startDateTime
			+ ",endDateTime=" + endDateTime
		    ;
	}
}
