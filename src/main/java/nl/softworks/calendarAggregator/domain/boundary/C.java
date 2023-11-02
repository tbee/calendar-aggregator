package nl.softworks.calendarAggregator.domain.boundary;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class C {
    static C singleton;

    @PostConstruct
    private void init() {
        C.singleton = this;
    }

    @Autowired
    ApplicationEventPublisher applicationEventPublisher;
    static public ApplicationEventPublisher applicationEventPublisher() {
        return C.singleton.applicationEventPublisher;
    }
}
