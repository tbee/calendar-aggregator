package nl.softworks.calendarAggregator.domain.service;

import jakarta.annotation.security.RolesAllowed;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class CalendarSourceService {
    private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceService.class);

    private static final ExecutorService executorService = Executors.newCachedThreadPool();
//    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);

    public void generateEvents() {
        generateEvents(null);
    }

    public void generateEvents(Runnable onStarted) {
        if (LOG.isInfoEnabled()) LOG.info("Generating events");

        // Only enabled ones
        List<CalendarSource> calendarSources = R.calendarSource().findAll().stream()
                .filter(cs -> cs.enabled())
                .toList();

        // Run in background
        try {
            List<Callable<Void>> callables = new ArrayList<>();
            for (CalendarSource calendarSourceLoop : calendarSources) {
                calendarSourceLoop.status("Generating");
                R.calendarSource().save(calendarSourceLoop);
                callables.add(() -> {
                    CalendarSource calendarSource = R.calendarSource().findById(calendarSourceLoop.id()).orElseThrow(); // otherwise the save may fail
                    try {
                        if (LOG.isInfoEnabled()) LOG.info("Generating events for " + calendarSource.name() + " in " + Thread.currentThread().getName());
                        calendarSource.generateEvents(null);
                    } catch (RuntimeException e) {
                        LOG.error("Problem generating events for " + calendarSource.name(), e);
                        calendarSource = R.calendarSource().findById(calendarSource.id()).orElse(null); // fresh does not work
                        calendarSource.status("Exception: " + e.getMessage());
                    }
                    finally {
                        R.calendarSource().save(calendarSource);
                        if (LOG.isInfoEnabled()) LOG.info("Generating events for " + calendarSource.name() + " done in " + Thread.currentThread().getName());
                    }
                    return null;
                });
            }
            executorService.invokeAll(callables, 1, TimeUnit.MINUTES); // TODO: tasks are not interrupted
            if (LOG.isInfoEnabled()) LOG.info("Generating events started");
            if (onStarted != null) {
                onStarted.run();
            }
        }
        catch (RuntimeException e) {
            LOG.error("Problem generating events", e);
            throw e;
        } catch (InterruptedException e) {
            LOG.error("Problem generating events", e);
            throw new RuntimeException(e);
        }
    }
}
