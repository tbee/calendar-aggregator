package nl.softworks.calendarAggregator.application.http.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "locations")
public record LocationsDTO(
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "location")
        List<LocationDTO> location
) {}