package nl.softworks.calendarAggregator;

import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;

public class CalendarAggregatorApplicationPostgres {

    public static void main(String[] args) throws Exception {
        if (!"restartedMain".equals(Thread.currentThread().getName())) {
            startPostgres();
        }
        CalendarAggregateApplication.main(args);
    }

    private static void startPostgres() throws IOException, InterruptedException {
        System.out.println("""
                1. Ensure Podman Desktop is Running.
                2. Enable Docker API in Podman Desktop:
                   - Open Podman Desktop
                   - Go to Settings > Docker Compatibility
                   - Ensure "Enable Docker API" is checked (it should be on by default)
                3. Set Environment Variables, either on your system or in the run configuration:
                   - DOCKER_HOST = "npipe:////./pipe/docker_engine"
                   - TESTCONTAINERS_RYUK_DISABLED = "true"
                """);
        // Disable HSQLDB
        System.setProperty("calendaraggregator.hsqldb.start", "false");

        // Start postgres container
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("password");
        postgres.start();
        System.out.println("Postgres container started on " + postgres.getJdbcUrl());

//        // restore database
//        postgres.copyFileToContainer(
//            MountableFile.forClasspathResource("dancemoments_pg_dump.sql"),
//            "/tmp/dump.sql"
//        );
//        postgres.execInContainer("psql", "-U", "postgres", "-d", "postgres", "-f", "/tmp/dump.sql");

        // Setup springboot
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());
        System.setProperty("spring.profiles.active", "dev");

        // Start applicatiion
    }
}
