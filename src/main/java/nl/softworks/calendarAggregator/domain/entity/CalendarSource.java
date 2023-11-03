package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "type")
public class CalendarSource extends EntityBase<CalendarSource> {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarSource.class);

	@Column(unique=true) // prevent the same name to occur
	@NotNull
	private String name;
	static public final String NAME_PROPERTYID = "name";
	public String getName() {
		return name;
	}
	public void setName(String v) {
		this.name = v;
	}

	@NotNull
	private String url;
	static public final String URL_PROPERTYID = "url";
	public String getUrl() {
		return url;
	}
	public void setUrl(String v) {
		this.url = v;
	}


	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "calendarSource", fetch = FetchType.EAGER)
    private final Set<CalendarEvent> calendarEvents = new HashSet<>();
	public Set<CalendarEvent> getCalendarEvents() {
		return Collections.unmodifiableSet(calendarEvents);
	}
	public void addCalendarEvent(CalendarEvent rosterDate) {
		calendarEvents.add(rosterDate);
		rosterDate.calendarSource = this;
	}

	public String toString() {
		return super.toString() //
		     + ",name=" + name
		     ;
	}
}
