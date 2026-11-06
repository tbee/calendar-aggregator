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
///
/// Trigger with:
///      http://localhost:8080/dev/dump
///
/// Or write the dump to a specific file directly:
///      http://localhost:8080/dev/dump?filename=dancemoments\_pg\_dump.sql
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
