package nl.softworks.calendarAggregator.domain.boundary;

import nl.softworks.calendarAggregator.domain.entity.Timezone;

import java.util.List;
import java.util.Optional;

public interface TimezoneRepo extends RepoBase<Timezone, Long> {
    List<Timezone> findAllByOrderByNameAsc();
    Optional<Timezone> findByName(String name);
}
