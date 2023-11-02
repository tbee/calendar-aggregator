package nl.softworks.calendarAggregator.domain.entity.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import nl.softworks.calendarAggregator.domain.entity.Person;

public class PersonPasswordValidatorImpl implements ConstraintValidator<PersonPasswordValidator, Person> {
    public void initialize(PersonPasswordValidator constraintAnnotation) {
        // initialize the zipcode/city/country correlation service
    }

    /**
     * Validate zipcode and city depending on the country
     */
    public boolean isValid(Person object, ConstraintValidatorContext context) {
        if (!(object instanceof Person)) {
            throw new IllegalArgumentException("@PersonPasswordValidator only applies to Person objects");
        }
        Person person = (Person) object;
        if (person.getPassword() == null || person.getPassword().isBlank()) {
            return false;
        }
        return true;
    }
}