package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class Label extends EntityBase<Label> {
	private static final Logger LOG = LoggerFactory.getLogger(Label.class);

	@Column(unique=true) // prevent the same name to occur
	@NotNull
	protected String name;
	static public final String NAME_PROPERTYID = "name";
	public String name() {
		return name;
	}
	public Label name(String v) {
		this.name = v;
		return this;
	}

	@NotNull
	protected String html;
	static public final String HTML_PROPERTYID = "html";
	public String html() {
		return html;
	}
	public Label html(String v) {
		this.html = v;
		return this;
	}

	public String toString() {
		return super.toString() //
		     + ",name=" + name
		     ;
	}
}

