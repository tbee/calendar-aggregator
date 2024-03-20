package nl.softworks.calendarAggregator.domain.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CalendarSourceMVELTest {

    static class CalendarSourceMVEL extends CalendarSource {

        @Override
        public String resolveUrl(String url) {
            return super.resolveUrl(url);
        }
    }

    @Test
    public void now() {
        CalendarSourceMVEL calendarSourceMVEL = new CalendarSourceMVEL();
        String expectedNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String expectedLastMonth = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // vanilla
        Assertions.assertEquals("yadda" + expectedNow, calendarSourceMVEL.resolveUrl("yadda@{java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern(\"yyyy-MM-dd\"))}"));
        // with predefined imports
        Assertions.assertEquals("yadda" + expectedNow, calendarSourceMVEL.resolveUrl("yadda@{LocalDateTime.now().format(DateTimeFormatter.ofPattern(\"yyyy-MM-dd\"))}"));
        // with predefined vars
        Assertions.assertEquals("yadda" + expectedNow, calendarSourceMVEL.resolveUrl("yadda@{now.format(yyyy_MM_dd)}"));
        // calculations
        Assertions.assertEquals("yadda" + expectedLastMonth, calendarSourceMVEL.resolveUrl("yadda@{now.minusMonths(1).format(yyyy_MM_dd)}"));
        // using predefined function
        Assertions.assertEquals("yadda" + expectedNow, calendarSourceMVEL.resolveUrl("yadda@{nowFormatted('yyyy-MM-dd')}"));
    }
}
