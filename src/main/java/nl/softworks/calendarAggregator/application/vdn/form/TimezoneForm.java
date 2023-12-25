package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.domain.entity.Timezone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimezoneForm extends AbstractCrudForm<Timezone> {
	private static final Logger LOG = LoggerFactory.getLogger(TimezoneForm.class);

	private final Binder<Timezone> binder = new Binder<>();

	private final TextField nameTextField = new TextField("Name");
	private final TextArea contentTextField = new TextArea("Content");

	public TimezoneForm() {
		contentTextField.setWidthFull();
		add(nameTextField, contentTextField);
		setColspan(contentTextField, 2);

		binder.forField(nameTextField).bind(Timezone::name, Timezone::name);
		binder.forField(contentTextField).bind(Timezone::content, Timezone::content);
	}

	@Override
	public TimezoneForm populateWith(Timezone timezone) {
		binder.readBean(timezone);
		return this;
	}

	@Override
	public TimezoneForm writeTo(Timezone timezone) throws ValidationException {
		binder.writeBean(timezone);
		return this;
	}
}