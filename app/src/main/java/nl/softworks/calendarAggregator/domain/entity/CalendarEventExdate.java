package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class CalendarEventExdate extends EntityBase<CalendarEventExdate> {

	public CalendarEventExdate() {
	}

	@ManyToOne
	@NotNull
	CalendarEvent calendarEvent;
	public @NotNull CalendarEvent calendarEvent() {
		return calendarEvent;
	}

	@NotNull
	private LocalDate excludedDate;
	static public final String EXCLUDEDDATE_PROPERTYID = "excludedDate";
	public LocalDate excludedDate() {
		return excludedDate;
	}
	public CalendarEventExdate excludedDate(LocalDate v) {
		this.excludedDate = v;
		return this;
	}

	@Override
	public String toString() {
		return super.toString() //
			+ ",startDateTime=" + excludedDate
		    ;
	}
}
