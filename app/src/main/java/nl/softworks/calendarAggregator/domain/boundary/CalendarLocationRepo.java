package nl.softworks.calendarAggregator.domain.boundary;

import nl.softworks.calendarAggregator.domain.entity.CalendarLocation;

import java.util.List;

public interface CalendarLocationRepo extends RepoBase<CalendarLocation, Long> {
    List<CalendarLocation> findAllByOrderByNameAsc();
}
