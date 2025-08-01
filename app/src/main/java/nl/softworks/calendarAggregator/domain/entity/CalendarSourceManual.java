package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import net.fortuna.ical4j.model.Recur;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collections;
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
	static public final String STARTDATETIME = "startDateTime";
	public LocalDateTime startDateTime() {
		return startDateTime;
	}
	public CalendarSourceManual startDateTime(LocalDateTime v) {
		this.startDateTime = v;
		return this;
	}

	@NotNull
	private LocalDateTime endDateTime;
	static public final String ENDDATETIME = "endDateTime";
	public LocalDateTime endDateTime() {
		return endDateTime;
	}
	public CalendarSourceManual endDateTime(LocalDateTime v) {
		this.endDateTime = v;
		return this;
	}

	@NotNull
	private String subject = "";
	static public final String SUBJECT = "subject";
	public String subject() {
		return subject;
	}

	public CalendarSourceManual subject(String v) {
		this.subject = v;
		return this;
	}

	@NotNull
	private String rrule = "";
	static public final String RRULE = "rrule";
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

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "calendarSource", fetch = FetchType.EAGER)
	protected final List<CalendarSourceManualExdate> calendarSourceManualExdates = new ArrayList<>();
	public List<CalendarSourceManualExdate> exdates() {
		return Collections.unmodifiableList(calendarSourceManualExdates);
	}
	public CalendarSourceManual exdates(List<CalendarSourceManualExdate> calendarEventExdates) {
		this.calendarSourceManualExdates.clear();
		calendarEventExdates.forEach(cee -> cee.calendarSource = this);
		this.calendarSourceManualExdates.addAll(calendarEventExdates);
		return this;
	}
	public void addExdate(CalendarSourceManualExdate v) {
		calendarSourceManualExdates.add(v);
		v.calendarSource = this;
	}

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "calendarSource", fetch = FetchType.EAGER)
	protected final List<CalendarSourceExtraEvent> calendarSourceExtraEvents = new ArrayList<>();
	public List<CalendarSourceExtraEvent> extraEvents() {
		return Collections.unmodifiableList(calendarSourceExtraEvents);
	}
	public CalendarSourceManual extraEvents(List<CalendarSourceExtraEvent> v) {
		this.calendarSourceExtraEvents.clear();
		v.forEach(cee -> cee.calendarSource = this);
		this.calendarSourceExtraEvents.addAll(v);
		return this;
	}

	@Override
	public List<CalendarEvent> generateEvents() {
		try {
			super.generateEvents();
			if (!isEnabled()) {
				return calendarEvents;
			}

			LocalDateTime now = LocalDateTime.now();
			if (rrule.isBlank()) {
				logAppend("No RRule, creating single event\n");
				calendarEvents.add(new CalendarEvent(CalendarSourceManual.this)
						.startDateTime(startDateTime)
						.endDateTime(endDateTime)
						.subject(subject));
			}
			else {
				logAppend("Applying RRule\n");
				calendarEvents.addAll(applyRRule(now));
				logAppend("Applied RRule: " + calendarEvents.size() + " events created\n");
			}

			// Extra events
			calendarEvents.addAll(calendarSourceExtraEvents.stream()
					.map(csmi -> new CalendarEvent(CalendarSourceManual.this)
						.subject(csmi.subject() == null || csmi.subject().isBlank() ? subject : csmi.subject())
						.startDateTime(csmi.startDateTime())
						.endDateTime(csmi.endDateTime()))
					.toList());

			// Nothing in the distant past
			dropExpiredEvents();
			sanatizeEvents();
			if (calendarEvents.isEmpty()) {
				status("No events");
			}

			return calendarEvents;
		}
		catch (RuntimeException e) {
			status(e.getMessage());
			StringWriter stringWriter = new StringWriter();
			e.printStackTrace(new PrintWriter(stringWriter));
			logAppend(stringWriter.toString());
			throw new RuntimeException(e);
		}
	}

	List<CalendarEvent> applyRRule(LocalDateTime now) { // Needed for testing

		// Exclude dates
		List<LocalDate> excludedLocalDates = calendarSourceManualExdates.stream().map(CalendarSourceManualExdate::excludedDate).toList();

		// Duration is needed to calculate end from start
		Duration duration = Duration.between(startDateTime, endDateTime);

		// Limit the amount of events generated
		LocalDateTime pastTreshold = now.minusMonths(1);
		LocalDateTime futureTreshold = now.plusMonths(5);

		// Create the recurrence rule
		Recur<LocalDateTime> recur = new Recur<>(rrule);
		LocalDateTime recurStartDateTime = startDateTime;
		LocalDateTime recurEndDateTime = toLocalDateTime(recur.getUntil());
		if (recurEndDateTime == null) {
			recurEndDateTime = futureTreshold;
		}
		List<LocalDateTime> localDateTimes = recur.getDates(recurStartDateTime, recurEndDateTime);

		// Convert dates to events
		return localDateTimes.stream()
				.map(ldt -> new CalendarEvent(CalendarSourceManual.this)
						.startDateTime(ldt)
						.endDateTime(ldt.plus(duration))
						.subject(CalendarSourceManual.this.subject))
				.filter(ce -> ce.startDateTime().isAfter(pastTreshold))
				.filter(ce -> ce.startDateTime.isBefore(futureTreshold))
				.filter(ce -> !excludedLocalDates.contains(ce.startDateTime.toLocalDate()))
				.toList();
	}

	private LocalDateTime toLocalDateTime(Temporal temporal) {
		if (temporal == null) {
			return null;
		}
		if (temporal instanceof OffsetDateTime offsetDateTime) {
			return offsetDateTime.toLocalDateTime();
		}
		if (temporal instanceof LocalDateTime localDateTime) {
			return localDateTime;
		}
		throw new IllegalArgumentException("Don't know how to convert " + temporal);
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
