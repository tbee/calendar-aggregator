package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.parameter.Value;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Entity
@DiscriminatorValue("manual")
public class CalendarSourceManual extends CalendarSource {

	public String type() {
		return "Manual";
	}

	public CalendarSourceManual() {
	}

	@NotNull
	private LocalDateTime startDateTime;
	static public final String STARTDATETIME_PROPERTYID = "startDateTime";
	public LocalDateTime startDateTime() {
		return startDateTime;
	}
	public CalendarSourceManual startDateTime(LocalDateTime v) {
		this.startDateTime = v;
		return this;
	}

	@NotNull
	private LocalDateTime endDateTime;
	static public final String ENDDATETIME_PROPERTYID = "endDateTime";
	public LocalDateTime endDateTime() {
		return endDateTime;
	}
	public CalendarSourceManual endDateTime(LocalDateTime v) {
		this.endDateTime = v;
		return this;
	}

	@NotNull
	private String subject = "";
	static public final String SUBJECT_PROPERTYID = "subject";
	public String subject() {
		return subject;
	}

	public CalendarSourceManual subject(String v) {
		this.subject = v;
		return this;
	}

	@NotNull
	private String rrule = "";
	static public final String RRULE_PROPERTYID = "rrule";
	public String rrule() {
		return rrule;
	}

	public CalendarSourceManual rrule(String v) {
		this.rrule = v;
		return this;
	}
	public boolean hasRrule() {
		return !rrule.isBlank();
	}

//	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "calendarEvent", fetch = FetchType.EAGER)
//	protected final List<CalendarEventExdate> calendarEventExdates = new ArrayList<>();
//	public List<CalendarEventExdate> calendarEventExdates() {
//		return Collections.unmodifiableList(calendarEventExdates);
//	}
//	public CalendarSourceManual calendarEventExdates(List<CalendarEventExdate> calendarEventExdates) {
//		this.calendarEventExdates.clear();
//		calendarEventExdates.forEach(cee -> cee.calendarEvent = this);
//		this.calendarEventExdates.addAll(calendarEventExdates);
//		return this;
//	}
//	public void addCalendarEventExdate(CalendarEventExdate rosterDate) {
//		calendarEventExdates.add(rosterDate);
//		rosterDate.calendarEvent = this;
//	}

	@Override
	public List<CalendarEvent> generateEvents(StringBuilder stringBuilder) {
		try {
			super.generateEvents(stringBuilder);
			if (!isEnabled()) {
				return calendarEvents;
			}

			LocalDateTime now = LocalDateTime.now();
			if (rrule.isBlank()) {
				if (stringBuilder != null) stringBuilder.append("No RRule, creating single event\n");
				calendarEvents.add(new CalendarEvent(CalendarSourceManual.this)
						.startDateTime(startDateTime)
						.endDateTime(endDateTime)
						.subject(subject));
			}
			else {
				if (stringBuilder != null) stringBuilder.append("Applying RRule\n");
				calendarEvents.addAll(applyRRule(now));
				if (stringBuilder != null) stringBuilder.append("Applied RRule: ").append(calendarEvents.size()).append(" events created\n");
			}

			// Nothing in the distant past
			LocalDateTime aFewDaysBack = now.minusDays(1).toLocalDate().atStartOfDay();
			calendarEvents.removeIf(ce -> ce.endDateTime().isBefore(aFewDaysBack));
			if (stringBuilder != null) stringBuilder.append("Filtered on after ").append(aFewDaysBack).append(", ").append(calendarEvents.size()).append(" events remaining\n");
			if (calendarEvents.isEmpty()) {
				status("No events");
			}

			return calendarEvents;
		}
		catch (RuntimeException e) {
			status(e.getMessage());
			if (stringBuilder != null) {
				StringWriter stringWriter = new StringWriter();
				e.printStackTrace(new PrintWriter(stringWriter));
				stringBuilder.append(stringWriter);
			}
			throw new RuntimeException(e);
		}
	}

	List<CalendarEvent> applyRRule(LocalDateTime now) { // Needed for testing

		// Exclude dates
		List<LocalDate> excludedLocalDates = List.of(); //calendarEventExdates().stream().map(CalendarEventExdate::excludedDate).toList();

		// Duration is needed to calculate end from start
		Duration duration = Duration.between(startDateTime, endDateTime);
		try {
			// Limit the amount of events generated
			LocalDateTime pastTreshold = now.minusMonths(1);
			LocalDateTime futureTreshold = now.plusMonths(5);

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
					.map(d -> new CalendarEvent(CalendarSourceManual.this)
							.startDateTime(toLocalDateTime(d))
							.endDateTime(toLocalDateTime(d).plus(duration))
							.subject(CalendarSourceManual.this.subject))
					.filter(ce -> ce.startDateTime.isAfter(pastTreshold))
					.filter(ce -> ce.startDateTime.isBefore(futureTreshold))
					.filter(ce -> !excludedLocalDates.contains(ce.startDateTime.toLocalDate()))
					.toList();
			return calendarEvents;
		}
		catch (ParseException e) {
			throw new RuntimeException(e);
		}
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
