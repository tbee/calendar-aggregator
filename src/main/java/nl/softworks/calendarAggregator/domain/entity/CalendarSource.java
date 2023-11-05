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
	public String name() {
		return name;
	}
	public CalendarSource name(String v) {
		this.name = v;
		return this;
	}

	@NotNull
	private String url;
	static public final String URL_PROPERTYID = "url";
	public String url() {
		return url;
	}
	public CalendarSource url(String v) {
		this.url = v;
		return this;
	}

	@NotNull
	private double lat;
	static public final String LAT_PROPERTYID = "lat";
	public double lat() {
		return lat;
	}

	public CalendarSource lat(double v) {
		if (v < -90 || v > 90) {
			throw new IllegalStateException("Latidude must be [-90,90]");
		}
		this.lat = v;
		return this;
	}

	@NotNull
	private double lon;
	static public final String LON_PROPERTYID = "lon";
	public double lon() {
		return lon;
	}
	public CalendarSource lon(double v) {
		if (v < -180 || v > 180) {
			throw new IllegalStateException("Latidude must be [-90,90]");
		}
		this.lon = v;
		return this;
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
