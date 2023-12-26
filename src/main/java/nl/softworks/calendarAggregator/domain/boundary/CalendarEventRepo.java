package nl.softworks.calendarAggregator.domain.boundary;

import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface CalendarEventRepo extends Repository<CalendarEvent, Long> {
    List<CalendarEvent> findAll();
}
