package nl.softworks.calendarAggregator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tbee.webstack.postgres.PostgresTestContainer;

import java.io.File;

/// Development-only endpoint to create a pg\_dump of the running test container database on demand.
/// Only active under the "dev" profile (which is set by the CalendarAggregatorApplicationTestContainer launcher).
///
/// Trigger with:
///      http://localhost:8080/dev/dump
///
/// Or write the dump to a specific file directly:
///      http://localhost:8080/dev/dump?filename=dancemoments\_pg\_dump.sql
///
/// Without a filename the dump is written to the current working directory (the project root when launched from
/// the IDE) under a timestamped name. A relative filename is resolved against that same working directory, and an
/// existing file is overwritten, so it can replace dancemoments\_pg\_dump.sql when desired.
@RestController
@Profile("dev")
public class DevDumpController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevDumpController.class);

    @GetMapping(value = "/dev/dump", produces = MediaType.TEXT_PLAIN_VALUE)
    public String dump(@RequestParam(required = false) String filename) {
        File written = (filename != null ? PostgresTestContainer.dump(new File(filename)) : PostgresTestContainer.dump());
        String message = "Dump written to " + written.getAbsolutePath() + "\n";
        LOGGER.info(message);
        return message;
    }
}
