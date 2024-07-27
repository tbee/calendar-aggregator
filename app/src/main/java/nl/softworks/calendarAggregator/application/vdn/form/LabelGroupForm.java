package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.domain.entity.LabelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LabelGroupForm extends AbstractCrudForm<LabelGroup> {
	private static final Logger LOGGER = LoggerFactory.getLogger(LabelGroupForm.class);

	private final Binder<LabelGroup> binder = new Binder<>();

	private final TextField nameTextField = new TextField("Name");

	public LabelGroupForm() {
		add(nameTextField);

		binder.forField(nameTextField).bind(LabelGroup::name, LabelGroup::name);
	}

	@Override
	public LabelGroupForm populateWith(LabelGroup v) {
		binder.readBean(v);
		return this;
	}

	@Override
	public LabelGroupForm writeTo(LabelGroup v) throws ValidationException {
		binder.writeBean(v);
		return this;
	}
}