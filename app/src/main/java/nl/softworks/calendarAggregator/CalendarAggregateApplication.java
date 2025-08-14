package nl.softworks.calendarAggregator;

import nl.softworks.calendarAggregator.application.jpa.RepoBaseImpl;
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

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = RepoBaseImpl.class)
public class CalendarAggregateApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarAggregateApplication.class);
	private static final AtomicBoolean hsqldbStarted = new AtomicBoolean(false);

	public static void main(String[] args) {
		Locale.setDefault(Locale.of("NL"));
		System.setProperty("liquibase.secureParsing", "false");
        System.setProperty("liquibase.duplicateFileMode", "WARN"); // springboot includes the db scripts twice
        System.setProperty("hsqldb.method_class_names", "org.jumpmind.symmetric.db.hsqldb.HsqlDbFunctions.*"); // for SymmetricDS
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
}
