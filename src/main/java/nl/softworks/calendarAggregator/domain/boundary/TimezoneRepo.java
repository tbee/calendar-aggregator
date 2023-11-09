package nl.softworks.calendarAggregator.domain.boundary;

import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.Timezone;
import org.springframework.data.domain.Example;

import java.util.Optional;

public interface TimezoneRepo extends RepoBase<Timezone, Long> {

    Optional<Timezone> findByName(String name);
}
