package nl.softworks.calendarAggregator.domain.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class S {
    static S singleton;

    @PostConstruct
    private void init() {
        S.singleton = this;
    }

    @Autowired
    CalendarSourceService rosterPeriodService;
    static public CalendarSourceService rosterPeriodService() {
        return S.singleton.rosterPeriodService;
    }
}
