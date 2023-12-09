package nl.softworks.calendarAggregator;

import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.service.CalendarSourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ApplicationEvents {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationEvents.class);

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    @Autowired
    private CalendarSourceService calendarSourceService;

    @EventListener(ApplicationReadyEvent.class)
    public void startApp() {

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime firstRun = now.toLocalDate().atStartOfDay().plusDays(1).plusHours(4); // 04:00
        if (LOG.isInfoEnabled()) LOG.info("Creating scheduled task for generating events, first run at " + firstRun);
        long delayUntilFirstRunInSeconds = Duration.between(now, firstRun).getSeconds();
        scheduledExecutorService.scheduleAtFixedRate(this::generateEvents, delayUntilFirstRunInSeconds, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS); // both values are in TimeUnit
    }

    private void generateEvents() {
        try {
            if (LOG.isInfoEnabled()) LOG.info("Scheduled task runs generate events");
            calendarSourceService.generateEvents();
        } catch (RuntimeException e) {
            LOG.error("Problem generating events in scheduled task", e);
        }
    }
}
