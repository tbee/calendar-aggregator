package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.domain.entity.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LabelForm extends AbstractCrudForm<Label> {
	private static final Logger LOG = LoggerFactory.getLogger(LabelForm.class);

	private final Binder<Label> binder = new Binder<>();

	private final TextField nameTextField = new TextField("Name");
	private final TextArea htmlTextField = new TextArea("Icon HTML");

	public LabelForm() {
		htmlTextField.setWidthFull();
		add(nameTextField, htmlTextField);
		setColspan(htmlTextField, 2);

		binder.forField(nameTextField).bind(Label::name, Label::name);
		binder.forField(htmlTextField).bind(Label::html, Label::html);
	}

	@Override
	public LabelForm populateWith(Label timezone) {
		binder.readBean(timezone);
		return this;
	}

	@Override
	public LabelForm writeTo(Label timezone) throws ValidationException {
		binder.writeBean(timezone);
		return this;
	}
}