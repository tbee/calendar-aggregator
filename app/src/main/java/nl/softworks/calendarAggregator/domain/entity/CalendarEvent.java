package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Comparator;
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
	static public final String STARTDATETIME = "startDateTime";
	public LocalDateTime startDateTime() {
		return startDateTime;
	}
	public CalendarEvent startDateTime(LocalDateTime v) {
		this.startDateTime = v;
		return this;
	}

	@NotNull
	private LocalDateTime endDateTime;
	static public final String ENDDATETIME = "endDateTime";
	public LocalDateTime endDateTime() {
		return endDateTime;
	}
	public CalendarEvent endDateTime(LocalDateTime v) {
		this.endDateTime = v;
		return this;
	}

	@NotNull
	private String subject = "";
	static public final String SUBJECT = "subject";
	public String subject() {
		return subject;
	}
	public String determineSubject() {
		if (subject != null && !subject.isBlank()) {
			return subject;
		}
		return calendarSource.description();
	}

	public CalendarEvent subject(String v) {
		this.subject = v;
		return this;
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

	/**
	 * Get the labels (taking subjectRegex into account)
	 *
	 * @return List (not Set, because the labels are sorted)
	 */
	public List<Label> labels() {
		String description = determineSubject();
		return calendarSource().labelAssignments().stream()
				.filter(la -> la.subjectRegexp().isBlank() || description.matches(la.subjectRegexp()))
				.map(CalendarSourceLabelAssignment::label)
				.sorted(Comparator.comparing(Label::seqnr))
				.toList();
	}
}
