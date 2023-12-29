package nl.softworks.calendarAggregator.testing.impl.restApi;


import nl.softworks.calendarAggregator.testing.TestContext;

public class CalendarResourceAPI extends API {

    static public CalendarResourceAPI get() {
        return TestContext.get(CalendarResourceAPI.class);
    }
}
