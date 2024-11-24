package nl.softworks.calendarAggregator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.tbee.webstack.hsqldb.HsqlEmbedded;

/**
 * Must be registered in META-INF/spring.factories like so:
 * org.springframework.context.ApplicationListener=nl.softworks.calendarAggregator.ApplicationPrepared
 */
public class ApplicationPrepared implements ApplicationListener<ApplicationPreparedEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationPrepared.class);

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();

        String url = environment.getProperty("spring.datasource.url");
        String username = environment.getProperty("spring.datasource.username");
        String password = environment.getProperty("spring.datasource.password");

        // Determine HSQLDB port
        if (!url.startsWith("jdbc:hsqldb:hsql://")) {
            LOGGER.warn("Not starting HSQLDB because URL seems to be of a different RDBMS: " + url);
            return;
        }
        String datasourceUrlSuffix = url.substring(url.lastIndexOf(":") + 1);
        int portnr = Integer.parseInt(datasourceUrlSuffix.substring(0, datasourceUrlSuffix.indexOf("/")));
        String dbname = datasourceUrlSuffix.substring(datasourceUrlSuffix.indexOf("/") + 1);

        // Start HSQLDB
        new HsqlEmbedded()
                .port(portnr)
                .username(username)
                .password(password)
                .database(dbname)
                .start();
    }
}
