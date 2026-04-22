package nl.softworks.calendarAggregator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.webstack.postgresql.PostgreSQLOnHsqldbCompatibilityDialect;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.nio.file.Path;

public class CalendarAggregatorApplicationPostgres {
    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarAggregatorApplicationPostgres.class);

    public static void main(String[] args) throws Exception {
        startPostgres("dancemoments", "dancemoments", "dancemoments", new File("app/dancemoments_pg_dump.sql"));
        System.setProperty("spring.jpa.database-platform", PostgreSQLOnHsqldbCompatibilityDialect.class.getName());
        System.setProperty("spring.profiles.active", "dev");

        CalendarAggregateApplication.main(args);
    }

    // TBEERNOT: move to webstack
    private static void startPostgres(String databaseName, String username, String password, File dumpFile) throws Exception {
        if (System.getProperty(CalendarAggregatorApplicationPostgres.class.getName()) != null) {
            LOGGER.info("Postgres container is already running");
            return;
        }

        // Start postgres container
        PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:18")
                .withDatabaseName(databaseName)
                .withUsername(username)
                .withPassword(password)
                .withLogConsumer(frame -> {
                    String line = frame.getUtf8String();
                    if (line != null && !line.isEmpty()) {
                        LOGGER.info("{postgreSQLContainer} " + line.replaceAll("\\r?\\n", ""));
                    }
                })
                .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forListeningPort());
        postgreSQLContainer.start();
        LOGGER.info("Postgres container started on " + postgreSQLContainer.getJdbcUrl());

        // Restore a database
        if (dumpFile != null) {
            LOGGER.info("Importing " + dumpFile.getAbsolutePath());
            String containerPath = "/tmp/dump.sql";
            postgreSQLContainer.copyFileToContainer(MountableFile.forHostPath(Path.of(dumpFile.getAbsolutePath())), containerPath);
            postgreSQLContainer.execInContainer("psql", "-U", username, "-d", databaseName, "-f", containerPath);
        }

        // Setup spring
        System.setProperty("spring.datasource.url", postgreSQLContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgreSQLContainer.getUsername());
        System.setProperty("spring.datasource.password", postgreSQLContainer.getPassword());

        // Mark as started
        System.setProperty(CalendarAggregatorApplicationPostgres.class.getName(), "started");
    }
}
