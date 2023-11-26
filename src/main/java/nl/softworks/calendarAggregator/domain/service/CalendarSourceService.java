package nl.softworks.calendarAggregator.domain.service;

import jakarta.annotation.security.RolesAllowed;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class CalendarSourceService {
    private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceService.class);

    public void generateEvents() {
        generateEvents(null);
    }

    public void generateEvents(Runnable onFinish) {
        if (LOG.isInfoEnabled()) LOG.info("Generating events");
        try {

            for (CalendarSource calendarSource : R.calendarSource().findAll()) {
                try {
                    if (LOG.isInfoEnabled()) LOG.info("Generating events for " + calendarSource.name());
                    calendarSource.generateEvents(null);
                } catch (RuntimeException e) {
                    LOG.error("Problem generating events for " + calendarSource.name(), e);
                    calendarSource = R.calendarSource().findById(calendarSource.id()).orElse(null);
                    calendarSource.status("Exception: " + e.getMessage());
                }
                R.calendarSource().save(calendarSource);
            }

            if (onFinish != null) {
                onFinish.run();
            }
        }
        catch (RuntimeException e) {
            LOG.error("Problem generating events", e);
            throw e;
        }
    }
}
