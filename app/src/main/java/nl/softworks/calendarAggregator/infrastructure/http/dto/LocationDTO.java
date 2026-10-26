package nl.softworks.calendarAggregator.infrastructure.http.dto;

import nl.softworks.calendarAggregator.domain.entity.CalendarLocation;

public record LocationDTO(long id, String name, String location, String url, double lat, double lon) {

    public LocationDTO(CalendarLocation calendarLocation) {
        this(calendarLocation.id(), calendarLocation.name(), calendarLocation.location(), calendarLocation.url(), calendarLocation.lat(), calendarLocation.lon());
    }
}
