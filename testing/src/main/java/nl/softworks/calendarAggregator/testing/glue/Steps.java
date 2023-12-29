package nl.softworks.calendarAggregator.testing.glue;

import com.microsoft.playwright.Page;
import nl.softworks.calendarAggregator.testing.Database;
import nl.softworks.calendarAggregator.testing.TestContext;
import org.junit.Assert;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

abstract public class Steps {

    protected TestContext testContext() {
        return TestContext.get();
    }

    protected Page page() {
        return TestContext.get().page();
    }
    protected Database database() {
        return TestContext.get().database();
    }

    protected void dataTableSetValue(Map<String, String> row, String colId, Consumer<String> consumer) {
        if (row.containsKey(colId)) {
            consumer.accept(row.get(colId));
        }
    }
    protected void dataTableAssertValue(Map<String, String> row, String colId, Supplier<String> supplier) {
        if (row.containsKey(colId)) {
            Assert.assertEquals(row.get(colId), supplier.get());
        }
    }
    protected boolean dataTableDoesNotContainOrValueIsEquals(Map<String, String> row, String colId, Supplier<String> supplier) {
        if (!row.containsKey(colId)) {
            return true;
        }
        return Objects.equals(row.get(colId), supplier.get());
    }
}
