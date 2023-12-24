package nl.softworks.calendarAggregator.domain.boundary;

import nl.softworks.calendarAggregator.domain.entity.Person;

import java.util.List;
import java.util.Optional;

public interface PersonRepo extends RepoBase<Person, Long> {
    Optional<Person> findByUsername(String username);
    List<Person> findByRoleAndEnabled(String role, boolean enabled);
}
