package nl.softworks.calendarAggregator.domain.service;

import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.FlyerCache;
import nl.softworks.calendarAggregator.domain.interfaces.FlyerCacheRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Cache of flyer extractions, keyed by image hash + prompt version + vision model, so the
 * same image is not sent to the vision model on every daily run.
 */
@Service
public class FlyerCacheService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlyerCacheService.class);

    @Autowired
    FlyerCacheRepo flyerCacheRepo;

    @Value("${calendaraggregator.aiAgent.flyerCacheRetentionDays:90}")
    private int cacheRetentionDays;

    @Value("${calendaraggregator.aiAgent.flyerEventRetentionDays:30}")
    private int eventRetentionDays;

    public Optional<FlyerCache> find(String imageHash, String promptVersion, String visionModel) {
        return flyerCacheRepo.findByImageHashAndPromptVersionAndVisionModel(imageHash, promptVersion, visionModel);
    }

    // store a successful extraction, or update an existing row
    public FlyerCache store(String imageHash, String promptVersion, String visionModel, String imageUrl,
                            String transcription, String eventsXml, List<CalendarEvent> events) {
        LocalDateTime now = LocalDateTime.now();
        FlyerCache flyerCache = find(imageHash, promptVersion, visionModel).orElseGet(FlyerCache::new);
        flyerCache.imageHash(imageHash)
                .promptVersion(promptVersion)
                .visionModel(visionModel)
                .imageUrl(imageUrl)
                .transcription(transcription)
                .eventsXml(eventsXml)
                .lastSeen(now)
                .latestEventEnd(latestEventEnd(events));
        if (flyerCache.firstSeen() == null) {
            flyerCache.firstSeen(now);
        }
        return flyerCacheRepo.save(flyerCache);
    }

    public void touch(FlyerCache flyerCache) {
        flyerCache.lastSeen(LocalDateTime.now());
        flyerCacheRepo.save(flyerCache);
    }

    // drop rows not seen for a while whose events have also passed
    @Transactional
    public void cleanup() {
        LocalDateTime now = LocalDateTime.now();
        int removed = flyerCacheRepo.deleteStale(
                now.minusDays(cacheRetentionDays),
                now.minusDays(eventRetentionDays));
        if (removed > 0) {
            LOGGER.info("Removed {} stale flyer cache entries", removed);
        }
    }

    private LocalDateTime latestEventEnd(List<CalendarEvent> events) {
        return events.stream()
                .map(CalendarEvent::endDateTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }
}