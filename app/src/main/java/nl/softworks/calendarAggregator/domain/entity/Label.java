package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class Label extends EntityBase<Label> {
	private static final Logger LOGGER = LoggerFactory.getLogger(Label.class);

	@Column(unique=true) // prevent the same name to occur
	@NotNull
	protected String name;
	static public final String NAME = "name";
	public String name() {
		return name;
	}
	public Label name(String v) {
		this.name = v;
		return this;
	}

	@NotNull
	protected String description;
	static public final String DESCRIPTION = "description";
	public String description() {
		return description;
	}
	public Label description(String v) {
		this.description = v;
		return this;
	}

	@NotNull
	protected String icon;
	static public final String ICON = "icon";
	public String icon() {
		return icon;
	}
	public Label icon(String v) {
		this.icon = v;
		return this;
	}

	@NotNull
	protected int seqnr = 0;
	static public final String SEQNR = "seqnr";
	public int seqnr() {
		return seqnr;
	}
	public Label seqnr(int v) {
		this.seqnr = v;
		return this;
	}

	@ManyToOne
	@NotNull
	private LabelGroup labelGroup;
	public LabelGroup labelGroup() {
		return labelGroup;
	}
	public Label labelGroup(LabelGroup v) {
		this.labelGroup = v;
		return this;
	}

//	@ManyToMany(mappedBy = "labels", fetch = FetchType.LAZY)
//	Set<CalendarSource> calendarSources;

	public String toString() {
		return super.toString() //
		     + ",name=" + name
		     ;
	}
}

