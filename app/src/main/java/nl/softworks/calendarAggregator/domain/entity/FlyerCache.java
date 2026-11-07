package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Cached extraction result for one flyer image. Keyed by content hash, prompt version and
 * vision model, so a changed image or an improved prompt forces a fresh read.
 */
@Entity
@Table(name = "flyer_cache", uniqueConstraints = @UniqueConstraint(
        name = "flyer_cache__key",
        columnNames = {"image_hash", "prompt_version", "vision_model"}))
public class FlyerCache extends EntityBase<FlyerCache> {

    @NotNull
    @Column(name = "image_hash", length = 64)
    private String imageHash;
    public String imageHash() {
        return imageHash;
    }
    public FlyerCache imageHash(String v) {
        this.imageHash = v;
        return this;
    }

    @NotNull
    @Column(name = "prompt_version", length = 40)
    private String promptVersion;
    public String promptVersion() {
        return promptVersion;
    }
    public FlyerCache promptVersion(String v) {
        this.promptVersion = v;
        return this;
    }

    @NotNull
    @Column(name = "vision_model", length = 255)
    private String visionModel;
    public String visionModel() {
        return visionModel;
    }
    public FlyerCache visionModel(String v) {
        this.visionModel = v;
        return this;
    }

    @Column(name = "image_url", length = 2000)
    private String imageUrl;
    public String imageUrl() {
        return imageUrl;
    }
    public FlyerCache imageUrl(String v) {
        this.imageUrl = v;
        return this;
    }

    @Lob
    @Column(name = "transcription")
    private String transcription;
    public String transcription() {
        return transcription;
    }
    public FlyerCache transcription(String v) {
        this.transcription = v;
        return this;
    }

    @Lob
    @Column(name = "events_xml")
    private String eventsXml;
    public String eventsXml() {
        return eventsXml;
    }
    public FlyerCache eventsXml(String v) {
        this.eventsXml = v;
        return this;
    }

    @Column(name = "first_seen")
    private LocalDateTime firstSeen;
    public LocalDateTime firstSeen() {
        return firstSeen;
    }
    public FlyerCache firstSeen(LocalDateTime v) {
        this.firstSeen = v;
        return this;
    }

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;
    public LocalDateTime lastSeen() {
        return lastSeen;
    }
    public FlyerCache lastSeen(LocalDateTime v) {
        this.lastSeen = v;
        return this;
    }

    @Column(name = "latest_event_end")
    private LocalDateTime latestEventEnd;
    public LocalDateTime latestEventEnd() {
        return latestEventEnd;
    }
    public FlyerCache latestEventEnd(LocalDateTime v) {
        this.latestEventEnd = v;
        return this;
    }

    @Override
    public String toString() {
        return super.toString() //
                + ",imageHash=" + imageHash
                ;
    }
}