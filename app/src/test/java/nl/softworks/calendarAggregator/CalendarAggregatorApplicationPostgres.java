package nl.softworks.calendarAggregator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.webstack.postgresql.PostgreSQLOnHsqldbCompatibilityDialect;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.nio.file.Path;

public class CalendarAggregatorApplicationPostgres {

    public static void main(String[] args) throws Exception {
        startPostgres(new File("app/dancemoments_pg_dump.sql"));
        System.setProperty("spring.profiles.active", "dev");
        CalendarAggregateApplication.main(args);
    }

    // TBEERNOT: move to webstack
    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarAggregatorApplicationPostgres.class);

    public static final String DATABASE_NAME = "dancemoments";
    public static final String USERNAME = "dancemoments";
    public static final String PASSWORD = "dancemoments";

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName(DATABASE_NAME)
            .withUsername(USERNAME)
            .withPassword(PASSWORD)
            .withLogConsumer(frame -> {
                String line = frame.getUtf8String();
                if (line != null && !line.isEmpty()) {
                    LOGGER.info("{POSTGRES} " + line.replaceAll("\\r?\\n", ""));
                }
            })
            .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forListeningPort());

    private static void startPostgres(File dumpFile) throws Exception {
        // This causes the container to be started twice because of the restartedMain thread: if (POSTGRES.isRunning()) {
        if (System.getProperty(CalendarAggregatorApplicationPostgres.class.getName()) != null) {
            LOGGER.info("Postgres container is already running");
            return;
        }

        // Start postgres container
        POSTGRES.start();
        LOGGER.info("Postgres container started on " + POSTGRES.getJdbcUrl());

        // restore database
        if (dumpFile != null) {
            LOGGER.info("Importing " + dumpFile.getAbsolutePath());
            String containerPath = "/tmp/dump.sql";
            POSTGRES.copyFileToContainer(MountableFile.forHostPath(Path.of(dumpFile.getAbsolutePath())), containerPath);
            POSTGRES.execInContainer("psql", "-U", USERNAME, "-d", DATABASE_NAME, "-f", containerPath);
        }

        // Setup spring
        System.setProperty("spring.datasource.url", POSTGRES.getJdbcUrl());
        System.setProperty("spring.datasource.username", POSTGRES.getUsername());
        System.setProperty("spring.datasource.password", POSTGRES.getPassword());
        System.setProperty("spring.jpa.database-platform", PostgreSQLOnHsqldbCompatibilityDialect.class.getName());

        // Mark as started
        System.setProperty(CalendarAggregatorApplicationPostgres.class.getName(), "started");
    }
}
