package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class CalendarEvent extends EntityBase<CalendarEvent> {

	@ManyToOne
	@NotNull
    CalendarSource calendarSource;

	@NotNull
	private LocalDateTime startDateTime;
	static public final String STARTDATETIME_PROPERTYID = "startDateTime";
	public LocalDateTime getStartDateTime() {
		return startDateTime;
	}
	public void setStartDateTime(LocalDateTime v) {
		this.startDateTime = v;
	}

	@NotNull
	private LocalDateTime endDateTime;
	static public final String ENDDATETIME_PROPERTYID = "endDateTime";
	public LocalDateTime getEndDateTime() {
		return endDateTime;
	}
	public void setEndDateTime(LocalDateTime v) {
		this.endDateTime = v;
	}

	private String subject;
	static public final String SUBJECT_PROPERTYID = "subject";
	public String getSubject() {
		return subject;
	}

	public void setSubject(String v) {
		this.subject = v;
	}

	@Override
	public String toString() {
		return super.toString() //
			+ ",startDateTime=" + startDateTime
			+ ",endDateTime=" + endDateTime
		    ;
	}
}
