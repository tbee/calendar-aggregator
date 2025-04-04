package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.domain.entity.LabelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.webstack.vdn.form.AbstractCrudFormLayout;

public class LabelGroupForm extends AbstractCrudFormLayout<LabelGroup> {
	private static final Logger LOGGER = LoggerFactory.getLogger(LabelGroupForm.class);

	private final Binder<LabelGroup> binder = new Binder<>();

	private final TextField nameTextField = new TextField("Name");
	private final TextField colorTextField = new TextField("Color");
	private final TextField backgroundTextField = new TextField("Background");

	public LabelGroupForm() {
		add(nameTextField, colorTextField, backgroundTextField);

		binder.forField(nameTextField).bind(LabelGroup::name, LabelGroup::name);
		binder.forField(colorTextField).bind(LabelGroup::color, LabelGroup::color);
		binder.forField(backgroundTextField).bind(LabelGroup::background, LabelGroup::background);
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