package nl.softworks.calendarAggregator.domain.interfaces;

import nl.softworks.calendarAggregator.domain.entity.FlyerCache;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface FlyerCacheRepo extends RepoBase<FlyerCache, Long> {

    Optional<FlyerCache> findByImageHashAndPromptVersionAndVisionModel(String imageHash, String promptVersion, String visionModel);

    // remove entries not seen recently whose events have also passed
    @Modifying
    @Transactional
    @Query("delete from FlyerCache f where f.lastSeen < :lastSeenBefore and (f.latestEventEnd is null or f.latestEventEnd < :eventEndBefore)")
    int deleteStale(@Param("lastSeenBefore") LocalDateTime lastSeenBefore, @Param("eventEndBefore") LocalDateTime eventEndBefore);
}