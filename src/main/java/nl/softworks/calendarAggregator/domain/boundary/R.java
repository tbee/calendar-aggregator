package nl.softworks.calendarAggregator.domain.boundary;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class R {
    static R singleton;

    @PostConstruct
    private void init() {
        R.singleton = this;
    }

    @Autowired
    CalendarSourceRepo calendarSourceRepo;
    static public CalendarSourceRepo rosterDateRepo() {
        return R.singleton.calendarSourceRepo;
    }

    @Autowired
    CalendarEventRepo calendarEventRepo;
    static public CalendarEventRepo rosterPeriodRepo() {
        return R.singleton.calendarEventRepo;
    }
}
