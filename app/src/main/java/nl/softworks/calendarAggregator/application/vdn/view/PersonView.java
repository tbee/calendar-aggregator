package nl.softworks.calendarAggregator.application.vdn.view;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import nl.softworks.calendarAggregator.application.vdn.form.PersonForm;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("/person")
@StyleSheet("context://../vaadin.css")
@RolesAllowed("ROLE_ADMIN")
public class PersonView extends AbstractCrudView<Person> {
	private static final Logger LOG = LoggerFactory.getLogger(PersonView.class);

	public PersonView() {
		super("Person"
				, Person::new
				, p -> R.person().save(p)
				, p -> R.person().delete(p)
				, () -> R.person().findAllByOrderByUsernameAsc()
				, PersonForm::new
				, grid -> {
					grid.addColumn(Person::username).setHeader("Username");
					grid.addColumn(Person::enabled).setHeader("Enabled");
				});
		tabs.setSelectedTab(personTab);
	}
}