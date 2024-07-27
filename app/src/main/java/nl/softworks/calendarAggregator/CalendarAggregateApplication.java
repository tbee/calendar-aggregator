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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TODO:
 */
@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = RepoBaseImpl.class)
public class CalendarAggregateApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarAggregateApplication.class);
	private static final AtomicBoolean hsqldbStarted = new AtomicBoolean(false);

	public static void main(String[] args) {
		//listCertificates();

		Locale.setDefault(new Locale("NL"));
		System.setProperty("liquibase.secureParsing", "false");
		SpringApplication.run(CalendarAggregateApplication.class, args);
	}

	private static void listCertificates() {
		try {
			// Load the JDK's cacerts keystore file
			String filename = System.getProperty("java.home") + "/lib/security/cacerts".replace('/', File.separatorChar);
			 if (LOGGER.isInfoEnabled())  LOGGER.info(filename);
			FileInputStream fileInputStream = new FileInputStream(filename);
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			String password = "changeit";
			keystore.load(fileInputStream, password.toCharArray());

			// This retrieves the most-trusted CAs from the keystore
			PKIXParameters params = new PKIXParameters(keystore);

			// Get the set of trust anchors, which contain the most-trusted CA certificates
            for (TrustAnchor trustAnchor : params.getTrustAnchors()) {
                X509Certificate x509Certificate = trustAnchor.getTrustedCert();
                 if (LOGGER.isInfoEnabled())  LOGGER.info(x509Certificate.getSubjectX500Principal().toString());
            }

			//new URL("https://wallznijkerk.trainin.app/api/v2/AWBW2/client/schedule?filter[view]=grid&filter[listing]=7092&filter[from]=2023-10-01&filter[until]=2025-01-01").openConnection().getContent();
		}
		catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | IOException e) {
			e.printStackTrace();
		}
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
