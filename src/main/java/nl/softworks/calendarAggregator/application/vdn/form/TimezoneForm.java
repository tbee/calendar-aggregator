package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import nl.softworks.calendarAggregator.application.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.domain.boundary.R;
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

	public TimezoneForm populateWith(Timezone timezone) {
		nameTextField.setValue(timezone.name() == null ? "" : timezone.name());
		contentTextField.setValue(timezone.content() == null ? "" : timezone.content());
		return this;
	}

	public TimezoneForm writeTo(Timezone timezone) {
		timezone.name(nameTextField.getValue());
		timezone.content(contentTextField.getValue());
		return this;
	}

	public static void showInsertDialog(Runnable onInsert) {
		Timezone timezone = new Timezone();
		TimezoneForm timezoneForm = new TimezoneForm().populateWith(timezone);
		new OkCancelDialog("Timezone", timezoneForm)
				.okLabel("Save")
				.onOk(() -> {
					timezoneForm.writeTo(timezone);
					R.timezone().save(timezone);
					onInsert.run();
				})
				.open();
	}
}