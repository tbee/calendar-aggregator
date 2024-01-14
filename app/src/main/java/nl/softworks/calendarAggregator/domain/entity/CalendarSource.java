package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import org.mvel2.MVEL;
import org.mvel2.ParserConfiguration;
import org.mvel2.ParserContext;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.jakarta.validator.UrlValidator;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "type")
@DiscriminatorValue("manual")
public class CalendarSource extends EntityBase<CalendarSource> {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarSource.class);

	public String type() {
		return "Manual";
	}

	@Column(unique=true) // prevent the same name to occur
	@NotNull
	protected String name;
	static public final String NAME_PROPERTYID = "name";
	public String name() {
		return name;
	}
	public CalendarSource name(String v) {
		this.name = v;
		return this;
	}

	@NotNull
	protected String location;
	static public final String LOCATION_PROPERTYID = "location";
	public String location() {
		return location;
	}
	public CalendarSource location(String v) {
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
	public CalendarSource url(String v) {
		this.url = v;
		return this;
	}

	@NotNull
	protected String status = "";
	static public final String STATUS_PROPERTYID = "status";
	public String status() {
		return status;
	}
	public CalendarSource status(String v) {
		this.status = v;
		return this;
	}

	@NotNull
	protected double lat;
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
	protected double lon;
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

	@NotNull
	@JoinColumn(name = "timezone_id", nullable = false)
	@ManyToOne(targetEntity=Timezone.class, fetch=FetchType.LAZY)
	protected Timezone timezone;
	static public final String TIMEZONE_PROPERTYID = "timezone";
	public Timezone timezone() {
		return timezone;
	}
	public CalendarSource timezone(Timezone v) {
		this.timezone = v;
		return this;
	}

	@NotNull
	protected boolean enabled = true;
	static public final String ENABLED_PROPERTYID = "enabled";
	public boolean enabled() {
		return enabled;
	}
	public CalendarSource enabled(boolean v) {
		this.enabled = v;
		return this;
	}

	private LocalDateTime lastRun;
	static public final String LASTRUN_PROPERTYID = "lastRun";
	public LocalDateTime lastRun() {
		return lastRun;
	}
	public CalendarSource lastRun(LocalDateTime v) {
		this.lastRun = v;
		return this;
	}

	@Lob
	private String log;
	static public final String LOG_PROPERTYID = "log";
	public String log() {
		return log;
	}
	public CalendarSource log(String v) {
		this.log = v;
		return this;
	}

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "calendarSource", fetch = FetchType.EAGER)
    protected final List<CalendarEvent> calendarEvents = new ArrayList<>();
	public List<CalendarEvent> calendarEvents() {
		return Collections.unmodifiableList(calendarEvents);
	}
	public void addCalendarEvent(CalendarEvent calendarEvent) {
		calendarEvents.add(calendarEvent);
		calendarEvent.calendarSource = this;
	}
	public void removeCalendarEvent(CalendarEvent calendarEvent) {
		calendarEvents.remove(calendarEvent);
		calendarEvent.calendarSource = null;
	}

	/**
	 * Using MVEL2 http://mvel.documentnode.com/
	 *
	 * @param url
	 * @param stringBuilder
	 * @return
	 */
	protected String resolveUrl(String url, StringBuilder stringBuilder) {
		if (stringBuilder != null) stringBuilder.append("URL before: " + url + "\n");

		// Predefined custom functions
		String functions =
    			"""
				def nowFormatted(format) {
				   java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern(format));
				}
				""";
		HashMap<String, Object> functionMap = new HashMap<>();
		MVEL.eval(functions, functionMap);

		// Compile the template, but in some opinionated context
		ParserConfiguration parserConfiguration = new ParserConfiguration();
		parserConfiguration.addPackageImport("java.time");
		parserConfiguration.addPackageImport("java.time.format");
		ParserContext context = new ParserContext(parserConfiguration);
		CompiledTemplate compiledExpression = TemplateCompiler.compileTemplate(url, context);

		// Evaluate the expression
		Map<String, Object> vars = new HashMap<>(functionMap);
		vars.put("now", LocalDateTime.now());
		vars.put("yyyy_MM_dd", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String newUrl = (String) TemplateRuntime.execute(compiledExpression, vars);
		if (stringBuilder != null) stringBuilder.append("URL after: " + newUrl + "\n");
		return newUrl;
	}

	protected String getUrl(String urlString) throws IOException, InterruptedException {
		try {
			HttpClient client = HttpClient.newBuilder()
					.version(HttpClient.Version.HTTP_1_1)
					.followRedirects(HttpClient.Redirect.NORMAL)
					.connectTimeout(Duration.ofMinutes(10))
					.build();
			HttpRequest request = HttpRequest.newBuilder()
					.GET()
					.uri(new URI(urlString))
					.build();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			return response.body();
		}
		catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	public List<CalendarEvent> generateEvents(StringBuilder stringBuilder) {
		status("ok");
		return calendarEvents();
	}

	public String toString() {
		return super.toString() //
		     + ",name=" + name
		     ;
	}
}

