package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.Label;
import nl.softworks.calendarAggregator.domain.entity.LabelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.webstack.vdn.form.AbstractCrudFormLayout;

public class LabelForm extends AbstractCrudFormLayout<Label> {
	private static final Logger LOGGER = LoggerFactory.getLogger(LabelForm.class);

	private final Binder<Label> binder = new Binder<>();

	private final TextField nameTextField = new TextField("Name");
	private final TextField descriptionTextField = new TextField("Description");
	private final TextField iconTextField = new TextField("Icon");
	private final IntegerField seqnrIntegerField = new IntegerField("Sequence");
	private final Select<LabelGroup> labelGroupSelect = new Select<>();

	public LabelForm() {
		setColspan(descriptionTextField, 2);
		add(nameTextField, iconTextField, descriptionTextField, seqnrIntegerField, labelGroupSelect);

		labelGroupSelect.setItems(new ListDataProvider<>(R.labelGroup().findAllByOrderByNameAsc()));
		labelGroupSelect.setRenderer(new ComponentRenderer<>(labelgroup -> {
			NativeLabel nativeLabel = new NativeLabel();
			nativeLabel.setText(labelgroup == null ? "-" : labelgroup.name());
			return nativeLabel;
		}));

		binder.forField(nameTextField).bind(Label::name, Label::name);
		binder.forField(descriptionTextField).bind(Label::description, Label::description);
		binder.forField(iconTextField).bind(Label::icon, Label::icon);
		binder.forField(seqnrIntegerField).bind(Label::seqnr, Label::seqnr);
		binder.forField(labelGroupSelect).bind(Label::labelGroup, Label::labelGroup);
	}

	@Override
	public LabelForm populateWith(Label label) {
		binder.readBean(label);
		return this;
	}

	@Override
	public LabelForm writeTo(Label label) throws ValidationException {
		binder.writeBean(label);
		return this;
	}
}