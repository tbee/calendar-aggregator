package nl.softworks.calendarAggregator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.webstack.postgresql.PostgreSQLOnHsqldbCompatibilityDialect;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import static org.testcontainers.containers.wait.strategy.Wait.forListeningPort;

public class CalendarAggregatorApplicationPostgres {
    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarAggregatorApplicationPostgres.class);

    public static void main(String[] args) throws Exception {
        startPostgres();
        CalendarAggregateApplication.main(args);
    }

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
            .waitingFor(forListeningPort());

    private static void startPostgres() throws Exception {
        if (System.getProperty("spring.datasource.url") != null) {
            LOGGER.info("Postgres container is already setup");
            return;
        }

        // Start postgres container
        POSTGRES.start();
        LOGGER.info("Postgres container started on " + POSTGRES.getJdbcUrl());

        // restore database
        POSTGRES.copyFileToContainer(
            MountableFile.forClasspathResource("dancemoments_pg_dump.sql"),
            "/tmp/dump.sql"
        );
        POSTGRES.execInContainer("psql", "-U", USERNAME, "-d", DATABASE_NAME, "-f", "/tmp/dump.sql");

        // Setup springboot
        System.setProperty("spring.datasource.url", POSTGRES.getJdbcUrl());
        System.setProperty("spring.datasource.username", POSTGRES.getUsername());
        System.setProperty("spring.datasource.password", POSTGRES.getPassword());
        System.setProperty("spring.profiles.active", "dev");
        System.setProperty("spring.jpa.database-platform", PostgreSQLOnHsqldbCompatibilityDialect.class.getName());
    }
}
