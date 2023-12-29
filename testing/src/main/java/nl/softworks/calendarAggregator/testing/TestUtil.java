package nl.softworks.calendarAggregator.testing;

import java.time.format.DateTimeFormatter;

public class TestUtil {
    final public static DateTimeFormatter DDMMYYYY = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /**
     * Sleep is only allowed to be used in a loop waiting for something else.
     * It may never be used to fix broken tests!!!
     *
     * @param ms
     */
    static public void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
