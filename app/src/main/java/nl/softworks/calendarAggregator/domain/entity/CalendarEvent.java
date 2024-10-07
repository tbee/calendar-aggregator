package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
		this.startDateTimeInZone = null;
		return this;
	}
	public LocalDateTime startDateTimeInZone(ZoneId viewerZoneId) {
		if (startDateTime == null) {
			return null;
		}
		if (startDateTimeInZone == null) {
			ZonedDateTime locationZDT = ZonedDateTime.of(startDateTime, calendarSource.determineTimezone().zoneId());
			ZonedDateTime viewerZDT = locationZDT.withZoneSameInstant(viewerZoneId);
			startDateTimeInZone = viewerZDT.toLocalDateTime();
		}
		return startDateTimeInZone;
	}
	@Transient
	private LocalDateTime startDateTimeInZone;

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
	public LocalDateTime endDateTimeInZone(ZoneId viewerZoneId) {
		if (endDateTime == null) {
			return null;
		}
		if (endDateTimeInZone == null) {
			ZonedDateTime locationZDT = ZonedDateTime.of(endDateTime, calendarSource.determineTimezone().zoneId());
			ZonedDateTime viewerZDT = locationZDT.withZoneSameInstant(viewerZoneId);
			endDateTimeInZone = viewerZDT.toLocalDateTime();
		}
		return endDateTimeInZone;
	}
	@Transient
	private LocalDateTime endDateTimeInZone;

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
		if (v != null && v.length() > 255) {
			v = v.substring(0, 255);
			LOGGER.warn("Subject truncated to 255 characters: " + v);
		}
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

	public boolean firstDateOfEvent(LocalDate pivotLocalDate) {
		return startDateTime.toLocalDate().equals(pivotLocalDate);
	}

	public boolean occursOn(LocalDate pivotLocalDate) {
		return !startDateTime.toLocalDate().isAfter(pivotLocalDate)
				&& !endDateTime.toLocalDate().isBefore(pivotLocalDate);
	}

	public List<LocalDate> eventDates() {
		// minus 1 minute to prevent 00:00 to skip over to the next day
		return startDateTime.toLocalDate().datesUntil(endDateTime.minusMinutes(1).toLocalDate().plusDays(1)).toList();
	}
}
