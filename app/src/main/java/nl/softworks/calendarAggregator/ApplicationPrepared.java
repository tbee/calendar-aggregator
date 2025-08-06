package nl.softworks.calendarAggregator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.tbee.webstack.hsqldb.HsqlEmbeddedServer;

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

        boolean hsqlStart = Boolean.parseBoolean(environment.getProperty("consilio.hsql.start", "true"));
        if (!hsqlStart) {
            if (LOGGER.isInfoEnabled()) LOGGER.info("HSQLDB is not started, as configured in consilio.hsql.start");
            return;
        }

        // Start HSQLDB
        new HsqlEmbeddedServer()
                .portFromUrl(url)
                .username(username)
                .password(password)
                .databaseFromUrl(url)
                .start();
    }
}
