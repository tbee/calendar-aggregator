package nl.softworks.calendarAggregator.application.vdn.view;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import nl.softworks.calendarAggregator.application.vdn.form.TimezoneForm;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.Timezone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("/timezone")
@StyleSheet("context://../vaadin.css")
@RolesAllowed("ROLE_ADMIN")
public class TimezoneView extends AbstractCrudView<Timezone> {
	private static final Logger LOG = LoggerFactory.getLogger(TimezoneView.class);

	public TimezoneView() {
		super("Timezone"
				, Timezone::new
				, p -> R.timezone().save(p)
				, p -> R.timezone().delete(p)
				, () -> R.timezone().findAllByOrderByNameAsc()
				, TimezoneForm::new
				, grid -> {
					grid.addColumn(Timezone::name).setHeader("Name");
				});
		tabs.setSelectedTab(timezoneTab);
	}
}