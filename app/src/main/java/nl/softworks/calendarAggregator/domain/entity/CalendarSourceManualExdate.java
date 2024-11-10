package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
public class CalendarSourceManualExdate extends EntityBase<CalendarSourceManualExdate> {

	public CalendarSourceManualExdate() {
	}

	@ManyToOne
	@NotNull
	CalendarSource calendarSource;
	public @NotNull CalendarSource calendarSource() {
		return calendarSource;
	}

	@NotNull
	private LocalDate excludedDate;
	static public final String EXCLUDEDDATE = "excludedDate";
	public LocalDate excludedDate() {
		return excludedDate;
	}
	public CalendarSourceManualExdate excludedDate(LocalDate v) {
		this.excludedDate = v;
		return this;
	}

	@Override
	public String toString() {
		return super.toString() //
			+ ",excludedDate=" + excludedDate
		    ;
	}
}
