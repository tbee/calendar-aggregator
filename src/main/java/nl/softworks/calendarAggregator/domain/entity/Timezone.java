package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

@Entity
public class Timezone extends EntityBase<Timezone> {

	@NotNull
	private String name;
	static public final String NAME_PROPERTYID = "subject";
	public String name() {
		return name;
	}
	public Timezone name(String v) {
		this.name = v;
		return this;
	}

	@NotNull
	private String content;
	static public final String CONTENT_PROPERTYID = "content";
	public String content() {
		return content;
	}
	public Timezone content(String v) {
		this.content = v;
		return this;
	}

	@Override
	public String toString() {
		return super.toString() //
			+ ",name=" + name
		    ;
	}
}
