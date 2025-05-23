package nl.softworks.calendarAggregator.application.vdn.view;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import nl.softworks.calendarAggregator.application.vdn.CalendarAggregatorAppLayout;
import nl.softworks.calendarAggregator.application.vdn.form.PersonForm;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.webstack.vdn.component.CrudComponent;

@Route("/person")
@StyleSheet("context://../vaadin.css")
@RolesAllowed("ROLE_ADMIN")
public class PersonView extends CalendarAggregatorAppLayout implements AfterNavigationObserver {
	private static final Logger LOGGER = LoggerFactory.getLogger(PersonView.class);

	private final CrudComponent<Person> crudComponent;

	public PersonView() {
		super("Person");
		tabs.setSelectedTab(personTab);
		setContent(crudComponent = new CrudComponent<>(getPageTitle()
				, Person::new
				, p -> R.person().save(p)
				, p -> R.person().delete(p)
				, () -> R.person().findAllByOrderByUsernameAsc()
				, PersonForm::new
				, grid -> {
					grid.addColumn(Person::username).setHeader("Username");
					grid.addColumn(Person::enabled).setHeader("Enabled");
				}));
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		crudComponent.reloadGrid();
	}
}