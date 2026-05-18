package nl.softworks.calendarAggregator.domain.boundary;

import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface CalendarEventRepo extends Repository<CalendarEvent, Long> {
    List<CalendarEvent> findAll();

    /// Optimized query that loads CalendarEvent with all related entities in one round-trip.
    /// This prevents N+1 queries when rendering calendar views.
    @Query("""
        SELECT DISTINCT ce
        FROM CalendarEvent ce
        JOIN FETCH ce.calendarSource cs
        JOIN FETCH cs.calendarLocation cl
        JOIN FETCH cl.timezone
        LEFT JOIN FETCH cs.labelAssignments la
        LEFT JOIN FETCH la.label l
        LEFT JOIN FETCH l.labelGroup
        """)
    List<CalendarEvent> findAllEager();
}
