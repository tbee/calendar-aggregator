package nl.softworks.calendarAggregator.domain.service;

import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class CalendarSourceService {
    private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceService.class);

    private static final ExecutorService executorService = Executors.newCachedThreadPool();
//    private static final ExecutorService executorServiceForTimeout = Executors.newSingleThreadExecutor(); //newCachedThreadPool();

    public void generateEvents() {
        generateEvents(null);
    }

    public void generateEvents(Runnable onStarted) {
        try {
            if (LOG.isInfoEnabled()) LOG.info("Scheduling event generation");
            Map<String, Future<?>> idToFutureMap = new TreeMap<>();
            R.calendarSource().findAll().stream()
                    .filter(cs -> cs.enabled())
                    .forEach(cs -> {
                        cs.status("Scheduled");
                        R.calendarSource().saveAndFlush(cs);
                        Future<?> future = executorService.submit(() -> generateEvents(cs.id()));
                        idToFutureMap.put(cs.name(), future);
                    });
            if (LOG.isInfoEnabled()) LOG.info("Event generation scheduled");
// Interrupting makes no difference; the task is not stopped.
//            executorServiceForTimeout.submit(() -> {
//                 interruptIfRunningTooLong(idToFutureMap);
//            });
            if (onStarted != null) {
                onStarted.run();
            }
        }
        catch (RuntimeException e) {
            LOG.error("Problem scheduling events", e);
            throw e;
        }
    }

    private static void interruptIfRunningTooLong(Map<String, Future<?>> idToFutureMap) {
        idToFutureMap.entrySet().forEach(i2f -> {
            if (LOG.isInfoEnabled()) LOG.info("Generating events, waiting for timeout " + i2f.getKey());
            try {
                i2f.getValue().get(3, TimeUnit.SECONDS);
                if (LOG.isInfoEnabled()) LOG.info("Generating events, completed within allotted time " + i2f.getKey());
            }
            catch (TimeoutException e) {
                if (LOG.isInfoEnabled()) LOG.info("Generating events, timed out " + i2f.getKey());
                i2f.getValue().cancel(true);
            }
            catch (InterruptedException | ExecutionException e) {
                if (LOG.isInfoEnabled()) LOG.info("Generating events, interrupted " + i2f.getKey());
            }
        });
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
