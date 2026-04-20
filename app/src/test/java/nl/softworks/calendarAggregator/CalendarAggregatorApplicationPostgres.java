package nl.softworks.calendarAggregator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

public class CalendarAggregatorApplicationPostgres {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationPrepared.class);

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("password")
            .withLogConsumer(frame -> {
                String line = frame.getUtf8String();
                if (line != null && !line.isEmpty()) {
                    LOGGER.info("{POSTGRES} " + line.replaceAll("\\r?\\n", ""));
                }
            });

    public static void main(String[] args) {
        startPostgres();
        CalendarAggregateApplication.main(args);
    }

    private static void startPostgres() {
        System.out.println("TBEERBNOT " + System.getProperty("spring.datasource.url"));
        if (System.getProperty("spring.datasource.url") != null) {
            LOGGER.info("Postgres container is already setup" + POSTGRES.getJdbcUrl());
            return;
        }

        // Start postgres container
        POSTGRES.start();
        LOGGER.info("Postgres container started on " + POSTGRES.getJdbcUrl());

//        // restore database
//        postgres.copyFileToContainer(
//            MountableFile.forClasspathResource("dancemoments_pg_dump.sql"),
//            "/tmp/dump.sql"
//        );
//        postgres.execInContainer("psql", "-U", "postgres", "-d", "postgres", "-f", "/tmp/dump.sql");

        // Setup springboot
        System.setProperty("spring.datasource.url", POSTGRES.getJdbcUrl());
        System.setProperty("spring.datasource.username", POSTGRES.getUsername());
        System.setProperty("spring.datasource.password", POSTGRES.getPassword());
        System.setProperty("spring.profiles.active", "dev");
        System.setProperty("spring.jpa.database-platform", "org.tbee.webstack.postgresql.CustomPostgreSQLDialect");
    }
}
