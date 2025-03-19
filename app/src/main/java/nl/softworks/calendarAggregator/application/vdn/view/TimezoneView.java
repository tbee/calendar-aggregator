package nl.softworks.calendarAggregator.application.vdn.view;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import nl.softworks.calendarAggregator.application.vdn.CalendarAggregatorAppLayout;
import nl.softworks.calendarAggregator.application.vdn.form.TimezoneForm;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.Timezone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.webstack.vdn.component.CrudComponent;

@Route("/timezone")
@StyleSheet("context://../vaadin.css")
@RolesAllowed("ROLE_ADMIN")
public class TimezoneView extends CalendarAggregatorAppLayout implements AfterNavigationObserver {
	private static final Logger LOGGER = LoggerFactory.getLogger(TimezoneView.class);

	private final CrudComponent<Timezone> crudComponent;

	public TimezoneView() {
		super("Timezone");
		tabs.setSelectedTab(timezoneTab);
		setContent(crudComponent = new CrudComponent<>(getPageTitle()
				, Timezone::new
				, p -> R.timezone().save(p)
				, p -> R.timezone().delete(p)
				, () -> R.timezone().findAllByOrderByNameAsc()
				, TimezoneForm::new
				, grid -> {
					grid.addColumn(Timezone::name).setHeader("Name");
				}));
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		crudComponent.reloadGrid();
	}
}