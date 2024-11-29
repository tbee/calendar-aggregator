package nl.softworks.calendarAggregator.application.jpa;

import jakarta.persistence.AttributeConverter;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceXmlScraper;

public class FormatConverter implements AttributeConverter<CalendarSourceXmlScraper.Format, String> {
    @Override
    public String convertToDatabaseColumn(CalendarSourceXmlScraper.Format format) {
        return format == null ? null : format.toString();
    }

    @Override
    public CalendarSourceXmlScraper.Format convertToEntityAttribute(String s) {
        return s == null ? null : CalendarSourceXmlScraper.Format.valueOf(s);
    }
}
