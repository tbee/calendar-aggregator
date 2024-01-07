package nl.softworks.calendarAggregator.testing.impl.db;

import nl.softworks.calendarAggregator.domain.entity.Person;
import nl.softworks.calendarAggregator.testing.TestContext;

import java.util.List;

public class PersonDB extends DB {

    static public PersonDB get() {
        return TestContext.get(PersonDB.class);
    }

    public Person selectByUsername(String username) {
        List<Person> persons = jdbi().select("select * from person where username = :username")
                .bind("username", username)
// issue because jdbi3 uses java.persistence not jakarta
//                .mapTo(Person.class)
//                .map(new JpaMapperFactory().build(Person.class, new ConfigRegistry()).orElseThrow())
                .map((rs, ctx) -> new Person()
                        .username(rs.getString("username"))
                        .email(rs.getString("email"))
                        .role(Person.Role.valueOf(rs.getString("role")))
                )
                .collectIntoList();
        return persons.isEmpty() ? null : persons.get(0);
    }

    public int insert(String username, String password) {
        String encoded = new Person().password(password).password();
        return jdbi().createUpdate("insert into person(username, password, email, role, enabled) values(:username, :password, :email, :role, :enabled)")
                .bind("username", username)
                .bind("password", encoded)
                .bind("email", username + "@test.com")
                .bind("role", Person.Role.ROLE_ADMIN.toString())
                .bind("enabled", true)
                .execute();
    }

    public int updatePassword(String username, String password) {
        String encoded = new Person().password(password).password();
        return jdbi().createUpdate("update person set password = :password where username = :username")
                .bind("username", username)
                .bind("password", encoded)
                .execute();
    }
}
