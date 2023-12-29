package nl.softworks.calendarAggregator.testing.impl.db;

import nl.softworks.calendarAggregator.testing.TestContext;

public class CalendarSourceDB extends DB {

    static public CalendarSourceDB get() {
        return TestContext.get(CalendarSourceDB.class);
    }

    public boolean checkIfExistsByName(String name) {
        int count = jdbi().createQuery("select count(*) from calendar_source where name = :name")
                .bind("name", name)
                .mapTo(Integer.class)
                .first().intValue();
        return count != 0;
    }
}
