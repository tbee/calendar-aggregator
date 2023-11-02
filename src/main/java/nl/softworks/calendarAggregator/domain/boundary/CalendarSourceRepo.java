package nl.softworks.calendarAggregator.domain.boundary;

import nl.softworks.calendarAggregator.boundary.jpa.CustomRepository;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;

public interface CalendarSourceRepo extends CustomRepository<CalendarSource, Long> {
}
