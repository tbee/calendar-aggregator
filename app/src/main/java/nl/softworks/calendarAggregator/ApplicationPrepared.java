package nl.softworks.calendarAggregator;

import org.apache.commons.io.IOUtils;
import org.hsqldb.persist.HsqlProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.File;
import java.io.IOException;
import java.net.URL;

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
        startHsqldbServer(url, username, password);
    }

    private void startHsqldbServer(String url, String username, String password) {

        // Determine HSQLDB port
        if (!url.startsWith("jdbc:hsqldb:hsql://")) {
            LOGGER.warn("Not starting HSQLDB because URL seems to be of a different RDBMS: " + url);
            return;
        }
        String datasourceUrlSuffix = url.substring(url.lastIndexOf(":") + 1);
        String portnr = datasourceUrlSuffix.substring(0, datasourceUrlSuffix.indexOf("/"));
        String dbname = datasourceUrlSuffix.substring(datasourceUrlSuffix.indexOf("/") + 1);

        HsqlProperties props = new HsqlProperties();
        props.setProperty("server.database.0", "file:hsqldb/" + dbname + ";user=" + username + ";password=" + password);
        props.setProperty("server.dbname.0", dbname);
        props.setProperty("server.port", portnr);
        props.setProperty("server.acl", createHsqldbAclFile());
        org.hsqldb.Server dbServer = new org.hsqldb.Server();
        try {
            dbServer.setProperties(props);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        dbServer.start();
        if (LOGGER.isInfoEnabled()) LOGGER.info("HSQLDB started");

        // Handle clean shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!dbServer.isNotRunning()) {
                if (LOGGER.isInfoEnabled()) LOGGER.info("HSQLDB shutting down");
                dbServer.shutdown();
                if (LOGGER.isInfoEnabled()) LOGGER.info("HSQLDB shutdown");
            }
        }));
    }

    private String createHsqldbAclFile() {
        try {
            File aclFile = File.createTempFile("hsqldb", "acl");
            aclFile.deleteOnExit();
            URL serverACLResourceURL = ApplicationPrepared.class.getResource("/hsqldb.acl");
            IOUtils.copy(serverACLResourceURL, aclFile);
            if (LOGGER.isInfoEnabled()) LOGGER.info("HSQLDB using " + aclFile.getAbsolutePath());
            return aclFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
