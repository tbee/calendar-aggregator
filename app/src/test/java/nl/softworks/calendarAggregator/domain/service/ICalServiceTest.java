package nl.softworks.calendarAggregator.domain.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ICalServiceTest {

    @Test
    public void dtStampTest() {
        Assertions.assertEquals("""
                DTSTAMP:20241004T145300Z\r
                DTSTAMP:20241005T064906Z\r
                """, new ICalService().sanatize("""
                DTSTAMP:20241004T145300
                DTSTAMP:20241005T064906Z
                """));
    }
}
