package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;

import java.time.ZoneId;

@Entity
public class Timezone extends EntityBase<Timezone> {

	@NotNull
	private String name;
	static public final String NAME = "subject";
	public String name() {
		return name;
	}
	public Timezone name(String v) {
		this.name = v;
		return this;
	}

	@NotNull
	private String content;
	static public final String CONTENT = "content";
	public String content() {
		return content;
	}
	public Timezone content(String v) {
		if (v != null) {
			v = v.trim();
			v = v + (v.endsWith("\n") ? "" : "\n");
		}
		this.content = v;
		return this;
	}

	public String ical() {
		return "BEGIN:VTIMEZONE\n"
				+ "TZID:" + name + "\n"
				+ content
				+ "END:VTIMEZONE\n";
	}

	public ZoneId zoneId() {
		return ZoneId.of(name);
	}

	@Override
	public String toString() {
		return super.toString() //
			+ ",name=" + name
		    ;
	}
}
