package nl.softworks.calendarAggregator;

import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;

public class CalendarAggregatorApplicationPostgres {

    private static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("password")
            .withLogConsumer(frame -> {
                String line = frame.getUtf8String();
                if (line != null && !line.isEmpty()) {
                    System.out.print("[postgres] " + line);
                }
            });

    public static void main(String[] args) throws Exception {
        if (!POSTGRES.isRunning()) {
            startPostgres();
        }
        CalendarAggregateApplication.main(args);
    }

    private static void startPostgres() throws IOException, InterruptedException {
        // Disable HSQLDB
        System.setProperty("calendaraggregator.hsqldb.start", "false");

        // Start postgres container
        POSTGRES.start();
        System.out.println("Postgres container started on " + POSTGRES.getJdbcUrl());

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

        // Start applicatiion
    }
}
