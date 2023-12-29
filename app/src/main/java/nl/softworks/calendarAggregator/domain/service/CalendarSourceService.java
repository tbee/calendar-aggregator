package nl.softworks.calendarAggregator.domain.service;

import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class CalendarSourceService {
    private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceService.class);

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public void generateEvents() {
        generateEvents(null);
    }

    public void generateEvents(Runnable onStarted) {
        try {
            if (LOG.isInfoEnabled()) LOG.info("Scheduling event generation");
            R.calendarSource().findAll().stream()
                    .filter(cs -> cs.enabled())
                    .forEach(cs -> {
                        if (onStarted != null) {
                            cs.status("Scheduled");
                            R.calendarSource().saveAndFlush(cs);
                        }
                        executorService.submit(() -> generateEvents(cs.id()));
                    });
            if (LOG.isInfoEnabled()) LOG.info("Event generation scheduled");
            if (onStarted != null) {
                onStarted.run();
            }
        }
        catch (RuntimeException e) {
            LOG.error("Problem scheduling events", e);
            throw e;
        }
    }

    private void generateEvents(long calendarSourceId) {
        // TODO: there must be a way to terminate / timeout a long running task
        try {
            CalendarSource calendarSource = R.calendarSource().findById(calendarSourceId).orElseThrow();
            try {
                if (LOG.isInfoEnabled()) LOG.info("Generating events for " + calendarSource.name() + " in " + Thread.currentThread().getName());
                StringBuilder stringBuilder = new StringBuilder();
                calendarSource.generateEvents(stringBuilder);
                calendarSource.log(stringBuilder.toString());
                if (LOG.isInfoEnabled()) LOG.info("Generating events for " + calendarSource.name() + " in " + Thread.currentThread().getName() + " finished");
            } catch (RuntimeException e) {
                LOG.error("Problem generating events for " + calendarSource.name(), e);
                calendarSource = R.calendarSource().findById(calendarSource.id()).orElseThrow(); // fresh does not work
                calendarSource.log(e.toString());
                calendarSource.status("Exception: " + e.getMessage());
            }
            calendarSource.lastRun(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            R.calendarSource().save(calendarSource);
        } catch (RuntimeException e) {
            LOG.error("Problem generating events for CalendarSource " + calendarSourceId, e);
        }
    }
}
