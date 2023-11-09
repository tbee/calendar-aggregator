package nl.softworks.calendarAggregator.boundary.vdn;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import nl.softworks.calendarAggregator.domain.entity.Timezone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimezoneForm extends FormLayout {
	private static final Logger LOG = LoggerFactory.getLogger(TimezoneForm.class);

	private final TextField nameTextField = new TextField("Name");
	private final TextArea contentTextField = new TextArea("Content");

	public TimezoneForm() {
		contentTextField.setWidthFull();
		add(nameTextField, contentTextField);
		setColspan(contentTextField, 2);
	}

	public void populateWith(Timezone timezone) {
		nameTextField.setValue(timezone == null ? "" : timezone.name());
		contentTextField.setValue(timezone == null ? "" : timezone.content());
	}

	public void writeTo(Timezone timezone) {
		timezone.name(nameTextField.getValue());
		timezone.content(contentTextField.getValue());
	}
}