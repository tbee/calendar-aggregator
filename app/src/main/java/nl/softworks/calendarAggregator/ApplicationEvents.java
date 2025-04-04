package nl.softworks.calendarAggregator;

import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.Person;
import nl.softworks.calendarAggregator.domain.service.GenerateEventsService;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ApplicationEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationEvents.class);

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    @Autowired
    private GenerateEventsService generateEventsService;

    @EventListener(ApplicationReadyEvent.class)
    public void startApp() {

        // Make sure there is an admin user
        List<Person> persons = R.person().findByRoleAndEnabled(Person.Role.ROLE_ADMIN, true);
        if (!persons.isEmpty()) {
             LOGGER.info("At least one administrator is present and active");
        }
        else {

            // Find a free username
            String username = "superuser";
            while (!R.person().findByUsername(username).isEmpty()) {
                username += "!";
            }

            // Create user
            String password = RandomStringUtils.random(32, true, true);
            LOGGER.warn("Creating an administrator user because none exist, note down this: " + username + " / " + password);
            R.person().save(new Person()
                    .username(username)
                    .password(password)
                    .role(Person.Role.ROLE_ADMIN)
                    .email("test@test.com")
                    .enabled(true));
        }

        // Create scheduler
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime firstRun = now.toLocalDate().atStartOfDay().plusDays(1); // 00:00 (this will make sure unexpected restarts do not skip running the task)
         if (LOGGER.isInfoEnabled())  LOGGER.info("Creating scheduled task for generating events, first run at " + firstRun);
        long delayUntilFirstRunInSeconds = Duration.between(now, firstRun).getSeconds();
        scheduledExecutorService.scheduleAtFixedRate(this::generateEvents, delayUntilFirstRunInSeconds, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS); // both values are in TimeUnit
    }

    private void generateEvents() {
        try {
             if (LOGGER.isInfoEnabled())  LOGGER.info("Scheduled task runs generate events");
            generateEventsService.generateEvents();
        } catch (RuntimeException e) {
            LOGGER.error("Problem generating events in scheduled task", e);
        }
    }
}
