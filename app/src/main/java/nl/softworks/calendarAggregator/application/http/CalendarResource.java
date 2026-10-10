package nl.softworks.calendarAggregator.application.http;

import nl.softworks.calendarAggregator.application.http.dto.LocationDTO;
import nl.softworks.calendarAggregator.application.http.dto.LocationsDTO;
import nl.softworks.calendarAggregator.domain.boundary.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/// OpenAPI specs:
/// - http://localhost:8080/openapi (JSON)
/// - http://localhost:8080/openapi.yaml
/// - http://localhost:8080/swagger-ui/index.html
@RestController
@RequestMapping("/api")
public class CalendarResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarResource.class);

    // example http://localhost:8080/location
    @GetMapping(path = {"/v1/location"})
    public LocationsDTO location(@RequestParam(defaultValue = "") Long locationId) {
        return new LocationsDTO(R.calendarLocation().findAll().stream()
                .filter(cl -> locationId == null || locationId.equals(cl.id()))
                .map(LocationDTO::new)
                .toList());
    }
}
