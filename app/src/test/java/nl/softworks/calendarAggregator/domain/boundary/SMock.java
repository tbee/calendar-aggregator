package nl.softworks.calendarAggregator.domain.boundary;

import nl.softworks.calendarAggregator.domain.control.GenerateEventsService;
import nl.softworks.calendarAggregator.domain.control.ICalService;

public class SMock {
    public static void populate() {
        S.singleton = new S();
        S.singleton.icalService = new ICalService();
        S.singleton.generateEventsService = new GenerateEventsService();
    }

}
