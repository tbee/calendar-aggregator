package nl.softworks.calendarAggregator.domain.service;

import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Service
public class GenerateEventsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateEventsService.class);

    private static final ThreadFactory virtualThreadFactory = Thread.ofVirtual().name("virtual-thread-", 0).factory(); // needed to name virtual threads
    private static final ExecutorService executorService = Executors.newThreadPerTaskExecutor(virtualThreadFactory);

    static {
        // Handle clean shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (LOGGER.isInfoEnabled())  LOGGER.info("GenerateEventsService shutting down");
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                    if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("GenerateEventsService did not terminate");
                    }
                }
                if (LOGGER.isInfoEnabled())  LOGGER.info("GenerateEventsService shutdown");
            }
            catch (InterruptedException e) {
                if (LOGGER.isInfoEnabled())  LOGGER.info("GenerateEventsService shutdown failure", e);
            }
        }));
    }

    public void generateEvents() {
        generateEvents(null);
    }

    public void generateEvents(Runnable onStarted) {
        try {
            if (LOGGER.isInfoEnabled()) LOGGER.info("Scheduling event generations");
            R.calendarLocation().findAll().stream()
                    .flatMap(cl -> cl.calendarSources().stream())
                    .forEach(cs -> {
                        if (LOGGER.isInfoEnabled()) LOGGER.info("Scheduled " + cs.calendarLocation().name());
                        cs.status("Scheduled")
                          .log("");
                        R.calendarSource().saveAndFlush(cs);
                        executorService.submit(() -> generateEvents(cs.id()));
                    });
            if (LOGGER.isInfoEnabled()) LOGGER.info("All event generations scheduled");
            if (onStarted != null) {
                onStarted.run();
            }
        }
        catch (RuntimeException e) {
            LOGGER.error("Problem scheduling events", e);
            throw e;
        }
    }

    private void generateEvents(long calendarSourceId) {
        // TODO: there must be a way to terminate / timeout a long running task
        try {
            CalendarSource calendarSource = R.calendarSource().findById(calendarSourceId).orElseThrow();
            try {
                if (LOGGER.isInfoEnabled()) LOGGER.info("Generating events for " + calendarSource.calendarLocation().name() + " in " + Thread.currentThread().getName());
                calendarSource.generateEvents();
                if (LOGGER.isInfoEnabled()) LOGGER.info("Generating events for " + calendarSource.calendarLocation().name() + " in " + Thread.currentThread().getName() + " finished");
            } catch (RuntimeException e) {
                LOGGER.error("Problem generating events for " + calendarSourceId + " " + calendarSource.calendarLocation().name(), e);
                calendarSource = R.calendarSource().findById(calendarSource.id()).orElseThrow(); // fresh does not work
                calendarSource.log(exceptionToString(e));
                calendarSource.status("Exception: " + e.getMessage());
            }
            calendarSource.lastRun(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            R.calendarSource().save(calendarSource);
        } catch (RuntimeException e) {
            // constraints are only validated upon save, so we need to handle and store those exceptions as well
            LOGGER.error("Problem generating events for CalendarSource " + calendarSourceId, e);
            CalendarSource calendarSource = R.calendarSource().findById(calendarSourceId).orElseThrow(); // fresh does not work
            calendarSource.log(exceptionToString(e));
            calendarSource.status("Exception: " + e.getMessage());
            R.calendarSource().save(calendarSource);
        }
    }

    private String exceptionToString(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
