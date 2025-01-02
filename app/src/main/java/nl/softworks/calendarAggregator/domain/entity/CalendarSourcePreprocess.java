package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

@Entity
public class CalendarSourcePreprocess extends EntityBase<CalendarSourcePreprocess> {

	@ManyToOne
	@NotNull
	CalendarSource calendarSource;
	public @NotNull CalendarSource calendarSource() {
		return calendarSource;
	}

	@NotNull
	private String oldValue;
	static public final String OLDVALUE = "oldValue";
	public String oldValue() {
		return oldValue;
	}
	public CalendarSourcePreprocess oldValue(String v) {
		this.oldValue = v;
		return this;
	}


	@NotNull
	private String newValue;
	static public final String newVALUE = "newValue";
	public String newValue() {
		return newValue;
	}
	public CalendarSourcePreprocess newValue(String v) {
		this.newValue = v;
		return this;
	}

	@Override
	public String toString() {
		return super.toString() //
			+ ",oldValue=" + oldValue
			+ ",newValue=" + newValue
		    ;
	}

	public String preprocess(String html) {
		if (LOGGER.isDebugEnabled()) LOGGER.debug("Preprocess: " + oldValue + " -> " + newValue);
		return html.replaceAll(oldValue, newValue);
	}
}
