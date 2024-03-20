package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.io.IOUtils;
import org.mvel2.MVEL;
import org.mvel2.ParserConfiguration;
import org.mvel2.ParserContext;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "type")
abstract public class CalendarSource extends EntityBase<CalendarSource> {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarSource.class);
	public static final String OK = "ok";

	transient protected Supplier<LocalDateTime> localDateTimeNowSupplier = () -> LocalDateTime.now();


	public String type() {
		return "Manual";
	}

	@ManyToOne
	@NotNull
	CalendarLocation calendarLocation;
	public CalendarLocation calendarLocation() {
		return calendarLocation;
	}


	protected String description = "";
	static public final String DESCRIPTION_PROPERTYID = "description";
	public String description() {
		return description == null ? "" : description;
	}
	public CalendarSource description(String v) {
		this.description = v;
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
	public boolean statusIsOk() {
		return OK.equals(status) || !isEnabled();
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
	public boolean isEnabled() {
		return enabled && calendarLocation.enabled;
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
	public void logAppend(String s) {
		log += s;
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
	protected String resolveUrl(String url) {
		logAppend("URL before: " + url + "\n");

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
		logAppend("URL after: " + newUrl + "\n");
		return newUrl;
	}

	protected String getUrl(String urlString) throws IOException, InterruptedException {

		// For unit tests
		if (urlString.startsWith("file:")) {
			return IOUtils.toString(new URL(urlString), StandardCharsets.UTF_8);
		}

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

	public List<CalendarEvent> generateEvents() {
		log = "";
		status(OK);
		calendarEvents.clear();
		return calendarEvents;
	}

	protected void dropExpiredEvents() {
		LocalDateTime aBitBack = localDateTimeNowSupplier.get().minusDays(1);
		calendarEvents.removeIf(ce -> ce.startDateTime().isBefore(aBitBack));
		logAppend("Dropped events before " + aBitBack + ", " + calendarEvents.size() + " events remaining\n");
	}

	protected void logMatcher(Matcher matcher, String content) {
		 logAppend("---\n");
		 logAppend("Start index: " + matcher.start() + "\n");
		 logAppend("End index: " + matcher.end() + "\n");
		 logAppend("Matched string: " + content + "," + matcher.start() + "," + matcher.end() + "\n");
		for (int i = 0; i < matcher.groupCount() + 1; i++) {
			 logAppend("Group " + i + " = " + matcher.group(i) + "\n");
		}
	}

	protected LocalDateTime makeSureEndIsAfterStart(LocalDateTime startLocalDateTime, LocalDateTime endLocalDateTime) {
		if (endLocalDateTime.isBefore(startLocalDateTime)) {
			endLocalDateTime = endLocalDateTime.plusDays(1); // This is to correct an end time that is on or after midnight
			logAppend("End moment < start moment, added one day: " + endLocalDateTime + "\n");
		}
		if (endLocalDateTime.isBefore(startLocalDateTime)) {
			throw new RuntimeException("End date should be after start: " + startLocalDateTime + " < " + endLocalDateTime);
		}
		return endLocalDateTime;
	}

	public String toString() {
		return super.toString() //
		     + ",type=" + type()
		     ;
	}
}

