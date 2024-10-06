package nl.softworks.calendarAggregator.domain.boundary;

import jakarta.annotation.PostConstruct;
import nl.softworks.calendarAggregator.domain.service.GenerateEventsService;
import nl.softworks.calendarAggregator.domain.service.ICalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Services, this can be used where Spring does not support injection
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class S {
    static S singleton;

    @PostConstruct
    private void init() {
        S.singleton = this;
    }

    @Autowired
    ICalService icalService;
    static public ICalService icalService() {
        return S.singleton.icalService;
    }

    @Autowired
    GenerateEventsService generateEventsService;
    static public GenerateEventsService generateEventsService() {
        return S.singleton.generateEventsService;
    }
}
