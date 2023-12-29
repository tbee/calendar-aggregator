package nl.softworks.calendarAggregator.application.jpa;

import jakarta.persistence.AttributeConverter;
import nl.softworks.calendarAggregator.domain.entity.Person;

public class PersonRoleConverter implements AttributeConverter<Person.Role, String> {
    @Override
    public String convertToDatabaseColumn(Person.Role role) {
        return role == null ? null : role.toString();
    }

    @Override
    public Person.Role convertToEntityAttribute(String s) {
        return s == null ? null : Person.Role.valueOf(s);
    }
}
