package nl.softworks.calendarAggregator.testing.impl.db;

import nl.softworks.calendarAggregator.testing.Database;
import nl.softworks.calendarAggregator.testing.TestContext;
import org.jdbi.v3.core.Handle;

public abstract class DB {

    protected Database database() {
        return TestContext.get().database();
    }

    protected Handle jdbi() {
        return TestContext.get().database().jdbi();
    }
}
