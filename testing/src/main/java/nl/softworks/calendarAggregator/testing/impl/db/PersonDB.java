package nl.softworks.calendarAggregator.testing.impl.db;

import nl.softworks.calendarAggregator.testing.TestContext;

public class PersonDB extends DB {

    static public PersonDB get() {
        return TestContext.get(PersonDB.class);
    }

    public boolean setPassword(String username, String password) {
//        new BCryptPasswordEncoder().encode(password);
        int count = jdbi().createQuery("select count(*) from calendar_source where username = :username")
                .bind("username", username)
                .mapTo(Integer.class)
                .first().intValue();
        return count != 0;
    }
}
