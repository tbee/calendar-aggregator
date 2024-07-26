package nl.softworks.calendarAggregator.domain.boundary;

import nl.softworks.calendarAggregator.domain.entity.Label;

import java.util.List;
import java.util.Optional;

public interface LabelRepo extends RepoBase<Label, Long> {
    List<Label> findAllByOrderByNameAsc();
    Optional<Label> findByName(String name);
}
