package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public class CalendarEvent extends EntityBase<CalendarEvent> {

	public CalendarEvent() {
	}

	CalendarEvent(CalendarSource calendarSource) {
		this.calendarSource = calendarSource;
	}

	@ManyToOne
	@NotNull
    CalendarSource calendarSource;
	public CalendarSource calendarSource() {
		return calendarSource;
	}

	@NotNull
	LocalDateTime startDateTime;
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

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "calendarEvent", fetch = FetchType.EAGER)
	protected final List<CalendarEventExdate> calendarEventExdates = new ArrayList<>();
	public List<CalendarEventExdate> calendarEventExdates() {
		return Collections.unmodifiableList(calendarEventExdates);
	}
	public CalendarEvent calendarEventExdates(List<CalendarEventExdate> calendarEventExdates) {
		this.calendarEventExdates.clear();
		calendarEventExdates.forEach(cee -> cee.calendarEvent = this);
		this.calendarEventExdates.addAll(calendarEventExdates);
		return this;
	}
	public void addCalendarEventExdate(CalendarEventExdate rosterDate) {
		calendarEventExdates.add(rosterDate);
		rosterDate.calendarEvent = this;
	}

	// Needed for testing
	@Override
	public String toString() {
		return super.toString() //
			+ ",startDateTime=" + startDateTime
			+ ",endDateTime=" + endDateTime
			+ ",subject=" + subject
		    ;
	}

	@AssertTrue
	public boolean isValid() {
		if (!startDateTime.isBefore(endDateTime)) {
			throw new ValidationException("Start (" + startDateTime + ") must be before end (" + endDateTime + ")");
		}
		return true;
	}
}
