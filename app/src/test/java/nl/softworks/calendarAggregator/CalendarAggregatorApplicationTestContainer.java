package nl.softworks.calendarAggregator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.webstack.postgres.PostgreSQLOnHsqldbCompatibilityDialect;
import org.tbee.webstack.postgres.PostgresTestContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.time.Duration;

public class CalendarAggregatorApplicationTestContainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarAggregatorApplicationTestContainer.class);

    public static void main(String[] args) throws Exception {

        // Start Postgres and restore the database dump in the root of the project
        new PostgresTestContainer()
                .database("dancemoments")
                .username("dancemoments")
                .password("dancemoments")
                .restore(new File("dancemoments_pg_dump.sql"))
                .log(LOGGER::info)
                .start();

        // Start Playwright MCP server
        // https://github.com/microsoft/playwright-mcp
        // --isolated gives every MCP client its own browser context, so concurrent scrapes do not share a page.
        GenericContainer<?> playwright = new GenericContainer<>(DockerImageName.parse("mcr.microsoft.com/playwright/mcp:v0.0.83"))
                .withExposedPorts(8931)
                .withCommand("--isolated", "--port", "8931", "--host", "0.0.0.0", "--allowed-hosts", "*", "--snapshot-mode", "none", "--idle-timeout", "900000")
                .withSharedMemorySize(1024L * 1024 * 1024)
                .waitingFor(Wait.forLogMessage(".*Listening on.*", 1))
                .withStartupTimeout(Duration.ofMinutes(3));
        playwright.start();
        LOGGER.info("Playwright MCP on port {}", playwright.getMappedPort(8931));
        System.setProperty("calendaraggregator.mcp.playwright.url", "http://localhost:" + playwright.getMappedPort(8931));

        System.setProperty("spring.jpa.database-platform", PostgreSQLOnHsqldbCompatibilityDialect.class.getName());
        System.setProperty("spring.profiles.active", "dev");

        CalendarAggregateApplication.main(args);
    }
}