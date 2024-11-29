package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "type")
abstract public class CalendarSource extends EntityBase<CalendarSource> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarSource.class);
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
	static public final String DESCRIPTION = "description";
	public String description() {
		return description == null ? "" : description;
	}
	public CalendarSource description(String v) {
		this.description = v;
		return this;
	}

	protected String url = "";
	static public final String URL = "url";
	public String url() {
		return url;
	}
	public CalendarSource url(String v) {
		this.url = v;
		return this;
	}
	public String determineUrl() {
		if (url != null && !url.isBlank()) {
			return url;
		}
		return calendarLocation.url;
	}


	@NotNull
	protected String status = "";
	static public final String STATUS = "status";
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
	static public final String ENABLED = "enabled";
	public boolean enabled() {
		return enabled;
	}
	public CalendarSource enabled(boolean v) {
		this.enabled = v;
		return this;
	}
	public boolean isEnabled() {
		return enabled && calendarLocation.isEnabled();
	}

	private LocalDateTime lastRun;
	static public final String LASTRUN = "lastRun";
	public LocalDateTime lastRun() {
		return lastRun;
	}
	public CalendarSource lastRun(LocalDateTime v) {
		this.lastRun = v;
		return this;
	}

	@Lob
	private String log;
	static public final String LOG = "log";
	public String log() {
		try {
			if (log == null || log.isBlank()) {
				return "";
			}

			byte[] bytes = Base64.getDecoder().decode(log);

			// Decompress
			GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes));
			InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String uncompressed = IOUtils.toString(bufferedReader);
			return uncompressed;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public CalendarSource log(String v) {
		try {
			if (v == null || v.isBlank()) {
				log = v;
				return this;
			}

			// Compress
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(v.length());
			GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
			gzipOutputStream.write(v.getBytes());
			gzipOutputStream.close();
			byte[] bytes = byteArrayOutputStream.toByteArray();
			byteArrayOutputStream.close();

			this.log = Base64.getEncoder().encodeToString(bytes);
			return this;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public void logAppend(String s) {
		log(log() + s);
	}

	@JoinColumn(name = "timezone_id", nullable = false)
	@ManyToOne(targetEntity=Timezone.class, fetch=FetchType.LAZY)
	protected Timezone timezone;
	static public final String TIMEZONE = "timezone";
	public Timezone timezone() {
		return timezone;
	}
	public CalendarSource timezone(Timezone v) {
		this.timezone = v;
		return this;
	}
	public Timezone determineTimezone() {
		if (timezone != null) {
			return timezone;
		}
		return calendarLocation.timezone();
	}

	@NotNull
	protected boolean hidden = false;
	static public final String HIDDEN = "hidden";
	public boolean hidden() {
		return hidden;
	}
	public CalendarSource hidden(boolean v) {
		this.hidden = v;
		return this;
	}
//	public boolean isHidden() {
//		return hidden();
//	}

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

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "calendarSource", fetch = FetchType.EAGER)
	protected final List<CalendarSourceLabelAssignment> labelAssignments = new ArrayList<>();
	public List<CalendarSourceLabelAssignment> labelAssignments() {
		return Collections.unmodifiableList(labelAssignments);
	}
	public void labelAssignments(Collection<CalendarSourceLabelAssignment> v) {
		// TODO this can be done more efficient
		labelAssignments.forEach(la -> la.calendarSource = null); // delete
		labelAssignments.clear();
		v.forEach(la -> la.calendarSource = this);
		labelAssignments.addAll(v);
	}
	public void addLabelAssignment(CalendarSourceLabelAssignment v) {
		labelAssignments.add(v);
	}
	public void removeLabelAssignment(CalendarSourceLabelAssignment v) {
		labelAssignments.remove(v);
	}
	public Set<Label> labels() {
		return labelAssignments.stream()
				.map(CalendarSourceLabelAssignment::label)
				.collect(Collectors.toSet());
	}

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "calendarSource", fetch = FetchType.EAGER)
	protected final List<CalendarSourcePreprocess> calendarSourcePreprocesses = new ArrayList<>();
	public List<CalendarSourcePreprocess> calendarSourcePreprocesses() {
		return Collections.unmodifiableList(calendarSourcePreprocesses);
	}
	public void calendarSourcePreprocesses(Collection<CalendarSourcePreprocess> v) {
		// TODO this can be done more efficient
		calendarSourcePreprocesses.forEach(la -> la.calendarSource = null); // delete
		calendarSourcePreprocesses.clear();
		v.forEach(la -> la.calendarSource = this);
		calendarSourcePreprocesses.addAll(v);
	}
	public void addPreprocess(CalendarSourcePreprocess v) {
		calendarSourcePreprocesses.add(v);
	}
	public void removePreprocess(CalendarSourcePreprocess v) {
		calendarSourcePreprocesses.remove(v);
	}


	/**
	 * Using MVEL2 http://mvel.documentnode.com/
	 *
	 * @param url
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
					.uri(new URI(urlString.trim()))
					.header("Accept", "*/*")
					//.header("Accept-Encoding", "gzip, deflate, br, zstd")
					.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
					.build();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			String content = response.body();

			// Preprocess
			for (CalendarSourcePreprocess calendarSourcePreprocess : calendarSourcePreprocesses) {
				content = calendarSourcePreprocess.preprocess(content);
			}
			//if (!calendarSourcePreprocesses.isEmpty()) logAppend("Preprocessed: " + html + "\n");

			return content;
		}
		catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	public List<CalendarEvent> generateEvents() {
		log("");
		status(OK);
		calendarEvents.clear();
		return calendarEvents;
	}

	protected void dropExpiredEvents() {
		LocalDateTime aBitBack = localDateTimeNowSupplier.get().minusDays(1);
		calendarEvents.removeIf(ce -> ce.startDateTime().isBefore(aBitBack));
		logAppend("Dropped events before " + aBitBack + ", " + calendarEvents.size() + " events remaining\n");
	}

	protected void sanatizeEvents() {
		for (CalendarEvent event : calendarEvents) {
			if (event.startDateTime().equals(event.endDateTime())) {
				event.endDateTime(event.endDateTime().plusMinutes(1));
				logAppend("Event " + event + " had identical start and end, added one minute to the end\n");
			}
		}
	}

	protected void logMatcher(Matcher matcher, String content) {
		logAppend("---\n");
		logAppend("Start index: " + matcher.start() + "\n");
		logAppend("End index: " + matcher.end() + "\n");
		logAppend("Matched string: " + content.substring(matcher.start(), matcher.end()) + "\n");
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
