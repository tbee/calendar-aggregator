package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.jakarta.validator.UrlValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public class CalendarLocation extends EntityBase<CalendarLocation> {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarLocation.class);

	@Column(unique=true) // prevent the same name to occur
	@NotNull
	protected String name;
	static public final String NAME_PROPERTYID = "name";
	public String name() {
		return name;
	}
	public CalendarLocation name(String v) {
		this.name = v;
		return this;
	}

	@NotNull
	protected String location;
	static public final String LOCATION_PROPERTYID = "location";
	public String location() {
		return location;
	}
	public CalendarLocation location(String v) {
		this.location = v;
		return this;
	}

	@NotNull
	@UrlValidator
	protected String url;
	static public final String URL_PROPERTYID = "url";
	public String url() {
		return url;
	}
	public CalendarLocation url(String v) {
		this.url = v;
		return this;
	}

	public String status() {
		String notOkStatus = calendarSources.stream()
				.filter(cs -> !cs.statusIsOk())
				.map(cs -> cs.status)
				.findFirst().orElse(null);
		return notOkStatus != null ? notOkStatus : CalendarSource.OK;
	}

	public boolean statusIsOk() {
		return calendarSources.stream()
				.filter(cs -> !cs.statusIsOk())
				.count() == 0;
	}

	@NotNull
	protected double lat;
	static public final String LAT_PROPERTYID = "lat";
	public double lat() {
		return lat;
	}
	public CalendarLocation lat(double v) {
		if (v < -90 || v > 90) {
			throw new IllegalStateException("Latidude must be [-90,90]");
		}
		this.lat = v;
		return this;
	}

	@NotNull
	protected double lon;
	static public final String LON_PROPERTYID = "lon";
	public double lon() {
		return lon;
	}
	public CalendarLocation lon(double v) {
		if (v < -180 || v > 180) {
			throw new IllegalStateException("Latidude must be [-90,90]");
		}
		this.lon = v;
		return this;
	}

	@NotNull
	@JoinColumn(name = "timezone_id", nullable = false)
	@ManyToOne(targetEntity=Timezone.class, fetch=FetchType.LAZY)
	protected Timezone timezone;
	static public final String TIMEZONE_PROPERTYID = "timezone";
	public Timezone timezone() {
		return timezone;
	}
	public CalendarLocation timezone(Timezone v) {
		this.timezone = v;
		return this;
	}

	@NotNull
	protected boolean enabled = true;
	static public final String ENABLED_PROPERTYID = "enabled";
	public boolean enabled() {
		return enabled;
	}
	public CalendarLocation enabled(boolean v) {
		this.enabled = v;
		return this;
	}

	private LocalDateTime lastRun;
	static public final String LASTRUN_PROPERTYID = "lastRun";
	public LocalDateTime lastRun() {
		return lastRun;
	}
	public CalendarLocation lastRun(LocalDateTime v) {
		this.lastRun = v;
		return this;
	}

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "calendarLocation", fetch = FetchType.EAGER)
    protected final List<CalendarSource> calendarSources = new ArrayList<>();
	public List<CalendarSource> calendarSources() {
		return Collections.unmodifiableList(calendarSources);
	}
	public void addCalendarSource(CalendarSource calendarSource) {
		calendarSources.add(calendarSource);
		calendarSource.calendarLocation = this;
	}
	public void removeCalendarSource(CalendarSource calendarSource) {
		calendarSources.remove(calendarSource);
		calendarSource.calendarLocation = null;
	}

	public List<CalendarEvent> generateEvents() {
		List<CalendarEvent> calendarEvents = new ArrayList<>();
		for (CalendarSource calendarSource : calendarSources) {
			calendarEvents.addAll(calendarSource.generateEvents());
		}
		return calendarEvents;
	}

	public String toString() {
		return super.toString() //
		     + ",name=" + name
		     ;
	}
}

