package nl.softworks.calendarAggregator;

import jakarta.annotation.PreDestroy;
import nl.softworks.calendarAggregator.application.jpa.RepoBaseImpl;
import org.hsqldb.persist.HsqlProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import sun.misc.Signal;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * TODO:
 */
@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = RepoBaseImpl.class)
public class CalendarAggregateApplication {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarAggregateApplication.class);

	public static void main(String[] args) {
		LOG.info("RESTART TRACING: application main called [" + Thread.currentThread().getName() + "]");
		Thread printingHook = new Thread(() -> {
			LOG.info("RESTART TRACING: application shutdown hook called");
			printStackTrace();
		});
		Runtime.getRuntime().addShutdownHook(printingHook);
		List<String> signals = Arrays.asList("SIGHUP", "SIGINT", "SIGQUIT", "SIGILL", "SIGTRAP", "SIGABRT", "SIGBUS", "SIGFPE", "SIGKILL", "SIGUSR1", "SIGSEGV", "SIGUSR2", "SIGPIPE", "SIGALRM", "SIGTERM", "SIGSTKFLT", "SIGCHLD", "SIGCONT", "SIGSTOP", "SIGTSTP", "SIGTTIN", "SIGTTOU", "SIGURG", "SIGXCPU", "SIGXFSZ", "SIGVTALRM", "SIGPROF", "SIGWINCH", "SIGIO", "SIGPWR", "SIGSYS", "SIGRTMIN", "SIGRTMIN+1", "SIGRTMIN+2", "SIGRTMIN+3", "SIGRTMIN+4", "SIGRTMIN+5", "SIGRTMIN+6", "SIGRTMIN+7", "SIGRTMIN+8", "SIGRTMIN+9", "SIGRTMIN+10", "SIGRTMIN+11", "SIGRTMIN+12", "SIGRTMIN+13", "SIGRTMIN+14", "SIGRTMIN+15", "SIGRTMAX-14", "SIGRTMAX-13", "SIGRTMAX-12", "SIGRTMAX-11", "SIGRTMAX-10", "SIGRTMAX-9", "SIGRTMAX-8", "SIGRTMAX-7", "SIGRTMAX-6", "SIGRTMAX-5", "SIGRTMAX-4", "SIGRTMAX-3", "SIGRTMAX-2", "SIGRTMAX-1", "SIGRTMAX");
		signals.forEach(s -> {
			try {
				Signal.handle(new Signal(s), signal -> {
					LOG.info("RESTART TRACING: signal called: " + signal.getName() + " (" + signal.getNumber() + ")");
				});
				LOG.info("RESTART TRACING: listening for signal " + s);
			}
			catch (IllegalArgumentException e) {
				LOG.info("RESTART TRACING: " + e.getMessage());
			}
		});

		Locale.setDefault(new Locale("NL"));
		System.setProperty("liquibase.secureParsing", "false");
		startHsqldbServer();
		SpringApplication.run(CalendarAggregateApplication.class, args);
	}

	@PreDestroy
	public void onExit() {
		LOG.info("RESTART TRACING: application onExit called");
		printStackTrace();
	}

	private static void printStackTrace() {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		new RuntimeException().printStackTrace(printWriter);
		printWriter.close();
		LOG.info(stringWriter.toString());
	}

	@Bean
	public LocaleResolver localeResolver() {
		CookieLocaleResolver localeResolver = new CookieLocaleResolver(); // Resolves the locale and stores it in a cookie stored on the user’s machine. https://lokalise.com/blog/spring-boot-internationalization/
		localeResolver.setDefaultLocale(Locale.getDefault());
		return localeResolver;
	}

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:messages");
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}

	static private void startHsqldbServer() {

		// Determine HSQLDB port
		Properties applicationProperties = new Properties();
		try (
                InputStream inputStream = CalendarAggregateApplication.class.getResourceAsStream("/application.properties");
		) {
			applicationProperties.load(inputStream);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		final String datasourceUrl = applicationProperties.getProperty("spring.datasource.url");
		if (!datasourceUrl.startsWith("jdbc:hsqldb:hsql://")) {
			LOG.warn("Not starting HSQLDB because URL seems to be of a different RDBMS: " + datasourceUrl);
			return;
		}
		String datasourceUrlSuffix = datasourceUrl.substring(datasourceUrl.lastIndexOf(":") + 1);
		String portnr = datasourceUrlSuffix.substring(0, datasourceUrlSuffix.indexOf("/"));
		String dbname = datasourceUrlSuffix.substring(datasourceUrlSuffix.indexOf("/") + 1);
		final String username = applicationProperties.getProperty("spring.datasource.username");
		final String password = applicationProperties.getProperty("spring.datasource.password");

		HsqlProperties props = new HsqlProperties();
	    props.setProperty("server.database.0", "file:hsqldb/" + dbname + ";user=" + username + ";password=" + password);
	    props.setProperty("server.dbname.0", dbname);
		props.setProperty("server.port", portnr);
	    org.hsqldb.Server dbServer = new org.hsqldb.Server();
	    try {
	        dbServer.setProperties(props);
	    } catch (Exception e) {
	    	e.printStackTrace();
	        return;
	    }
	    dbServer.start();
		if (LOG.isInfoEnabled()) LOG.info("HSQLDB started");

		// Handle clean shutdown
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (!dbServer.isNotRunning()) {
				if (LOG.isInfoEnabled()) LOG.info("HSQLDB shutting down");
				dbServer.shutdown();
				if (LOG.isInfoEnabled()) LOG.info("HSQLDB shutdown");
			}
		}));
	}
}
