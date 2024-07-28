package nl.softworks.calendarAggregator.domain.boundary;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Repositories
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class R {
    static R singleton;

    @PostConstruct
    private void init() {
        R.singleton = this;
    }

    @Autowired
    PersonRepo personRepo;
    static public PersonRepo person() {
        return R.singleton.personRepo;
    }

    @Autowired
    CalendarLocationRepo calendarLocationRepo;
    static public CalendarLocationRepo calendarLocation() {
        return R.singleton.calendarLocationRepo;
    }

    @Autowired
    CalendarSourceRepo calendarSourceRepo;
    static public CalendarSourceRepo calendarSource() {
        return R.singleton.calendarSourceRepo;
    }

    @Autowired
    CalendarEventRepo calendarEventRepo;
    static public CalendarEventRepo calendarEvent() {
        return R.singleton.calendarEventRepo;
    }

    @Autowired
    TimezoneRepo timezoneRepo;
    static public TimezoneRepo timezone() {
        return R.singleton.timezoneRepo;
    }

    @Autowired
    SettingsRepo settingsRepo;
    static public SettingsRepo settings() {
        return R.singleton.settingsRepo;
    }

    @Autowired
    LabelGroupRepo labelGroupRepo;
    static public LabelGroupRepo labelGroup() {
        return R.singleton.labelGroupRepo;
    }

    @Autowired
    LabelRepo labelRepo;
    static public LabelRepo label() {
        return R.singleton.labelRepo;
    }

    @Autowired
    CalendarSourceLabelAssignmentRepo calendarSourceLabelAssignmentRepo;
    static public CalendarSourceLabelAssignmentRepo calendarSourceLabelAssignment() {
        return R.singleton.calendarSourceLabelAssignmentRepo;
    }
}
