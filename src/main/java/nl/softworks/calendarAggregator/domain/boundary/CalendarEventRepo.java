package nl.softworks.calendarAggregator.domain.boundary;

import nl.softworks.calendarAggregator.boundary.jpa.CustomRepository;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;

public interface CalendarEventRepo extends CustomRepository<CalendarEvent, Long> {
}
