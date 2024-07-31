package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "calendar_source2label")
public class CalendarSourceLabelAssignment extends EntityBase<CalendarSourceLabelAssignment> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarSourceLabelAssignment.class);

	public CalendarSourceLabelAssignment() {}

	public CalendarSourceLabelAssignment(CalendarSource calendarSource, Label label) {
		this.calendarSource = calendarSource;
		this.label = label;
	}

	@ManyToOne
	@NotNull
	CalendarSource calendarSource;
	static public final String CALENDARSOURCE = "calendarSource";
	public CalendarSource calendarSource() {
		return calendarSource;
	}
	public CalendarSourceLabelAssignment calendarSource(CalendarSource v) {
		this.calendarSource = v;
		return this;
	}

	@ManyToOne
	@NotNull
	private Label label;
	static public final String LABEL = "label";
	public Label label() {
		return label;
	}
	public CalendarSourceLabelAssignment label(Label v) {
		this.label = v;
		return this;
	}

	@NotNull
	private String subjectRegexp = "";
	static public final String SUBJECTREGEXP = "subjectRegexp";
	public String subjectRegexp() {
		return subjectRegexp;
	}
	public CalendarSourceLabelAssignment subjectRegexp(String v) {
		this.subjectRegexp = v;
		return this;
	}

	public String toString() {
		return super.toString() //
		     + ",calendarSource=" + (calendarSource == null ? "null" : calendarSource.type())
			 + ",label=" + (label == null ? "null" : label.name())
		     ;
	}
}

