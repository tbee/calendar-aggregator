package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import nl.softworks.calendarAggregator.domain.entity.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.webstack.vdn.form.AbstractCrudFormLayout;

public class PersonForm extends AbstractCrudFormLayout<Person> {
	private static final Logger LOGGER = LoggerFactory.getLogger(PersonForm.class);

	private final Binder<Person> binder = new Binder<>();
	private final TextField usernameTextField = new TextField("Username");
	private final PasswordField passwordTextField = new PasswordField("Password (empty is no change)");
	private final TextField emailTextField = new TextField("Email");
	private final ComboBox<Person.Role> roleComboBox = new ComboBox<>();
	private final Checkbox enabledCheckbox = new Checkbox("Enabled");

	public PersonForm() {
		roleComboBox.setItemLabelGenerator(r -> r.toString().replace("ROLE_", ""));
		roleComboBox.setRenderer(new ComponentRenderer<>(r -> {
			Span span = new Span(r.toString().replace("ROLE_", ""));
			return span;
		}));
		roleComboBox.setItems(Person.Role.values());

		add(usernameTextField, passwordTextField, emailTextField, roleComboBox, enabledCheckbox);

		binder.forField(usernameTextField).bind(Person::username, Person::username);
		binder.forField(emailTextField).bind(Person::email, Person::email);
		binder.forField(roleComboBox).bind(Person::role, Person::role);
		binder.forField(enabledCheckbox).bind(Person::enabled, Person::enabled);
	}

	@Override
	public PersonForm populateWith(Person person) {
		binder.readBean(person);
		return this;
	}

	@Override
	public PersonForm writeTo(Person person) throws ValidationException {
		binder.writeBean(person);
		if (!passwordTextField.isEmpty()) {
			person.password(passwordTextField.getValue());
		}
		return this;
	}
}