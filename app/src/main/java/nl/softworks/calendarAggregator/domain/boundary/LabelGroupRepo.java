package nl.softworks.calendarAggregator.domain.boundary;

import nl.softworks.calendarAggregator.domain.entity.LabelGroup;

import java.util.List;
import java.util.Optional;

public interface LabelGroupRepo extends RepoBase<LabelGroup, Long> {
    List<LabelGroup> findAllByOrderByNameAsc();
    Optional<LabelGroup> findByName(String name);
}
