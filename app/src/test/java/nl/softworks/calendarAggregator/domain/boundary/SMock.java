package nl.softworks.calendarAggregator.domain.boundary;

import nl.softworks.calendarAggregator.domain.service.GenerateEventsService;
import nl.softworks.calendarAggregator.domain.service.ICalService;

public class SMock {
    public static void populate() {
        S.singleton = new S();
        S.singleton.icalService = new ICalService();
        S.singleton.generateEventsService = new GenerateEventsService();
    }

}
