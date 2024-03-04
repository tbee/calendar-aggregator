package nl.softworks.calendarAggregator;

import nl.softworks.calendarAggregator.application.jpa.RepoBaseImpl;
import org.apache.commons.io.IOUtils;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TODO:
 */
@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = RepoBaseImpl.class)
public class CalendarAggregateApplication {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarAggregateApplication.class);
	private static final AtomicBoolean hsqldbStarted = new AtomicBoolean(false);

	public static void main(String[] args) {
		//listCertificates();
		//printStackTraces();

		Locale.setDefault(new Locale("NL"));
		System.setProperty("liquibase.secureParsing", "false");
		startHsqldbServer();
		SpringApplication.run(CalendarAggregateApplication.class, args);
	}

	private static void listCertificates() {
		try {
			// Load the JDK's cacerts keystore file
			String filename = System.getProperty("java.home") + "/lib/security/cacerts".replace('/', File.separatorChar);
			if (LOG.isInfoEnabled()) LOG.info(filename);
			FileInputStream fileInputStream = new FileInputStream(filename);
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			String password = "changeit";
			keystore.load(fileInputStream, password.toCharArray());

			// This retrieves the most-trusted CAs from the keystore
			PKIXParameters params = new PKIXParameters(keystore);

			// Get the set of trust anchors, which contain the most-trusted CA certificates
            for (TrustAnchor trustAnchor : params.getTrustAnchors()) {
                X509Certificate x509Certificate = trustAnchor.getTrustedCert();
                if (LOG.isInfoEnabled()) LOG.info(x509Certificate.getSubjectX500Principal().toString());
            }

			//new URL("https://wallznijkerk.trainin.app/api/v2/AWBW2/client/schedule?filter[view]=grid&filter[listing]=7092&filter[from]=2023-10-01&filter[until]=2025-01-01").openConnection().getContent();
		}
		catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | IOException e) {
			e.printStackTrace();
		}
    }

	private static void printStackTraces() {
		LOG.info("printStackTraces");
		Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
		for (Thread thread : allStackTraces.keySet()) {
			StackTraceElement[] stackTraceElements = allStackTraces.get(thread);
			if (stackTraceElements.length > 0) {
				LOG.info("==========");
			}
			for (java.lang.StackTraceElement stackTraceElement : stackTraceElements) {
				LOG.info("[" + thread.getName() + "] " + stackTraceElement.toString());
			}
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

	static private void startHsqldbServer() {
		// If already started, bail
		if (hsqldbStarted.getAndSet(true)) { // Restart patch
			return;
		}

		// Determine HSQLDB port
		Properties applicationProperties = loadApplicationProperties();
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
		props.setProperty("server.acl", createHsqldbAclFile());
	    org.hsqldb.Server dbServer = new org.hsqldb.Server();
	    try {
	        dbServer.setProperties(props);
	    }
		catch (Exception e) {
	    	throw new RuntimeException(e);
	    }
	    dbServer.start();
		if (LOG.isInfoEnabled()) LOG.info("HSQLDB started");

		// Handle clean shutdown
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			hsqldbStarted.set(false);
			if (!dbServer.isNotRunning()) {
				if (LOG.isInfoEnabled()) LOG.info("HSQLDB shutting down");
				dbServer.shutdown();
				if (LOG.isInfoEnabled()) LOG.info("HSQLDB shutdown");
			}
		}));
	}

	private static Properties loadApplicationProperties() {
		Properties properties = new Properties();
		try (
			InputStream inputStream = CalendarAggregateApplication.class.getResourceAsStream("/application.properties");
		) {
			properties.load(inputStream);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		return properties;
	}

	private static String createHsqldbAclFile() {
        try {
            File aclFile = File.createTempFile("hsqldb", "acl");
			aclFile.deleteOnExit();
			URL serverACLResourceURL = CalendarAggregateApplication.class.getResource("/hsqldb.acl");
			IOUtils.copy(serverACLResourceURL, aclFile);
			if (LOG.isInfoEnabled()) LOG.info("HSQLDB using " + aclFile.getAbsolutePath());
			return aclFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
}
