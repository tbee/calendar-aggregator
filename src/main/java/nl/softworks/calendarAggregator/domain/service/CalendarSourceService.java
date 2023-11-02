package nl.softworks.calendarAggregator.domain.service;

import jakarta.annotation.security.RolesAllowed;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CalendarSourceService {
    private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceService.class);

    @Transactional
    @RolesAllowed("ROLE_PLANNER")
    public void addRosterPeriod(CalendarSource rosterPeriod) {
    }
}
