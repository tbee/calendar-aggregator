package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class LabelGroup extends EntityBase<LabelGroup> {
	private static final Logger LOGGER = LoggerFactory.getLogger(LabelGroup.class);

	@Column(unique=true) // prevent the same name to occur
	@NotNull
	protected String name;
	static public final String NAME = "name";
	public String name() {
		return name;
	}
	public LabelGroup name(String v) {
		this.name = v;
		return this;
	}

	@NotNull
	protected String color;
	static public final String COLOR = "color";
	public String color() {
		return color;
	}
	public LabelGroup color(String v) {
		this.color = v;
		return this;
	}

	public String toString() {
		return super.toString() //
		     + ",name=" + name
		     ;
	}
}

