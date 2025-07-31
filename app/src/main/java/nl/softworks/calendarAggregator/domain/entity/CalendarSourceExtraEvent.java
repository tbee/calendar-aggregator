package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
public class CalendarSourceExtraEvent extends EntityBase<CalendarSourceExtraEvent> {

	@ManyToOne
	@NotNull
	CalendarSource calendarSource;
	public @NotNull CalendarSource calendarSource() {
		return calendarSource;
	}

	@NotNull
	private LocalDateTime startDateTime;
	static public final String STARTDATETIME = "startDateTime";
	public LocalDateTime startDateTime() {
		return startDateTime;
	}
	public CalendarSourceExtraEvent startDateTime(LocalDateTime v) {
		this.startDateTime = v;
		return this;
	}

	@NotNull
	private LocalDateTime endDateTime;
	static public final String ENDDATETIME = "endDateTime";
	public LocalDateTime endDateTime() {
		return endDateTime;
	}
	public CalendarSourceExtraEvent endDateTime(LocalDateTime v) {
		this.endDateTime = v;
		return this;
	}

//	@NotNull
	private String subject;
	static public final String SUBJECT = "subject";
	public String subject() {
		return subject;
	}
	public CalendarSourceExtraEvent subject(String v) {
		this.subject = v;
		return this;
	}

	@Override
	public String toString() {
		return super.toString() //
			+ ",startDateTime=" + startDateTime
			+ ",endDateTime=" + endDateTime
			+ ",subject=" + subject
		    ;
	}
}
