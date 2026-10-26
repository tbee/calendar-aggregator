package nl.softworks.calendarAggregator.infrastructure.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

/**
 * Development-only endpoint to create a pg_dump of the running test container database on demand.
 * Only active under the "dev" profile (which is set by the CalendarAggregatorApplicationPostgres launcher).
 * <p>
 * Trigger with: http://localhost:8080/dev/dump
 * <p>
 * The dump is written to the current working directory (the project root when launched from the IDE),
 * so it can replace dancemoments_pg_dump.sql when desired. Generated files match the
 * {@code dancemoments_*.sql} git-ignore pattern.
 */
@RestController
@Profile("dev")
public class DevDumpController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevDumpController.class);

    @GetMapping(value = "/dev/dump", produces = MediaType.TEXT_PLAIN_VALUE)
    public String dump() {
        // Reference the class only here so the app packaged without testcontainers still loads this controller.
        File written = org.tbee.webstack.postgres.PostgresTestContainer.dump();
        String message = "Dump written to " + written.getAbsolutePath();
        LOGGER.info(message);
        return message + "\n";
    }
}
