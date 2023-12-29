package nl.softworks.calendarAggregator.domain.boundary;

import nl.softworks.calendarAggregator.domain.entity.CalendarSource;

import java.util.List;

public interface CalendarSourceRepo extends RepoBase<CalendarSource, Long> {
    List<CalendarSource> findAllByOrderByNameAsc();
}
