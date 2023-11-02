package nl.softworks.calendarAggregator;

import nl.softworks.calendarAggregator.boundary.jpa.CustomRepositoryImpl;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * TODO:
 */
@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = CustomRepositoryImpl.class)
public class CalendarAggregateApplication {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarAggregateApplication.class);

	public static void main(String[] args) {
		Locale.setDefault(new Locale("NL"));
		System.setProperty("liquibase.secureParsing", "false");
		startHsqldbServer();
		SpringApplication.run(CalendarAggregateApplication.class, args);
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
		String datasourceUrl = applicationProperties.getProperty("spring.datasource.url");
		if (!datasourceUrl.startsWith("jdbc:hsqldb:hsql://")) {
			LOG.warn("Not starting HSQLDB because URL seems to be of a different RDBMS: " + datasourceUrl);
			return;
		}
		datasourceUrl = datasourceUrl.substring(datasourceUrl.lastIndexOf(":") + 1);
		String portnr = datasourceUrl.substring(0, datasourceUrl.indexOf("/"));
		String dbname = datasourceUrl.substring(datasourceUrl.indexOf("/") + 1);

		HsqlProperties props = new HsqlProperties();
	    props.setProperty("server.database.0", "file:hsqldb/" + dbname + ";");
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
