package nl.softworks.calendarAggregator.testing.glue;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import nl.softworks.calendarAggregator.testing.TestContext;

import java.util.List;
import java.util.Map;

public class CalendarSourceAndEventSteps extends Steps {

    public static final String NAME = "Name";

    @Given("^source exist$")
    public void exist(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
//            dataTableSetValue(row, NAME, s -> thing.setIdentificationNo(s));
        }
        if (rows.size() == 1 && rows.get(0).containsKey(NAME)) {
            TestContext.get().lastVisitedCalendarSourceName(rows.get(0).get(NAME));
        } else {
            TestContext.get().lastVisitedCalendarSourceName(null);
        }
    }
}
