package nl.softworks.calendarAggregator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.webstack.postgres.PostgreSQLOnHsqldbCompatibilityDialect;
import org.tbee.webstack.postgres.PostgresTestContainer;

import java.io.File;

public class CalendarAggregatorApplicationPostgres {
    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarAggregatorApplicationPostgres.class);

    public static void main(String[] args) throws Exception {
        new PostgresTestContainer()
                .database("dancemoments")
                .username("dancemoments")
                .password("dancemoments")
                .load(new File("app/dancemoments_pg_dump.sql"))
                .log(LOGGER::info)
                .start();
        System.setProperty("spring.jpa.database-platform", PostgreSQLOnHsqldbCompatibilityDialect.class.getName());
        System.setProperty("spring.profiles.active", "dev");

        CalendarAggregateApplication.main(args);
    }
}
