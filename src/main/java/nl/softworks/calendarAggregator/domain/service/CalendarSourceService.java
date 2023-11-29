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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class CalendarSourceService {
    private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceService.class);


    public void generateEvents() {
        generateEvents(null);
    }

    public void generateEvents(Runnable onFinish) {
        if (LOG.isInfoEnabled()) LOG.info("Generating events");
        List<CalendarSource> calendarSources = R.calendarSource().findAll();
        ExecutorService executorService = Executors.newCachedThreadPool();
        CountDownLatch countDownLatch = new CountDownLatch(calendarSources.size());
        try {
            for (CalendarSource calendarSourceLoop : calendarSources) {
                executorService.submit(() -> {
                    CalendarSource calendarSource = calendarSourceLoop;
                    try {
                        if (LOG.isInfoEnabled()) LOG.info("Generating events for " + calendarSource.name() + " in " + Thread.currentThread().getName());
                        calendarSource.generateEvents(null);
                    } catch (RuntimeException e) {
                        LOG.error("Problem generating events for " + calendarSource.name(), e);
                        calendarSource = R.calendarSource().findById(calendarSource.id()).orElse(null); // fresh does not work
                        calendarSource.status("Exception: " + e.getMessage());
                    }
                    R.calendarSource().save(calendarSource);
                    if (LOG.isInfoEnabled()) LOG.info("Generating events for " + calendarSource.name() + " done in " + Thread.currentThread().getName());
                    countDownLatch.countDown();
                });
            }
            countDownLatch.await(60, TimeUnit.SECONDS);
            if (LOG.isInfoEnabled()) LOG.info("Generating events complete");
            if (onFinish != null) {
                onFinish.run();
            }
        }
        catch (InterruptedException e) {
            LOG.error("Problem generating events", e);
            throw new RuntimeException(e);
        }
        catch (RuntimeException e) {
            LOG.error("Problem generating events", e);
            throw e;
        }
        finally {
            executorService.shutdown();
        }
    }
}
