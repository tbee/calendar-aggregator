package nl.softworks.calendarAggregator.domain.boundary;

import nl.softworks.calendarAggregator.domain.entity.Label;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LabelRepo extends RepoBase<Label, Long> {
    List<Label> findAllByOrderByNameAsc();
    List<Label> findAllByOrderBySeqnrAsc();
    Optional<Label> findByName(String name);


    /// Optimized query that loads Label with all related entities in one round-trip.
    /// This prevents N+1 queries when rendering calendar views.
    @Query("""
        SELECT DISTINCT l
        FROM Label l
        JOIN FETCH l.labelGroup lg
        """)
    List<Label> findAllByOrderBySeqnrAscEager();

}
