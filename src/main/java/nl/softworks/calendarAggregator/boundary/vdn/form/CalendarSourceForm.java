package nl.softworks.calendarAggregator.boundary.vdn.form;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import nl.softworks.calendarAggregator.boundary.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.Timezone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarSourceForm extends FormLayout {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceForm.class);

	private final TextField nameTextField = new TextField("Name");
	private final TextField urlTextField = new TextField("Url");
	private final NumberField latNumberField = new NumberField("LAT");
	private final NumberField lonNumberField = new NumberField("LON");
	private final ComboBox<Timezone> timezoneComboBox = new ComboBox<>("Timezone");

	public CalendarSourceForm() {
		timezoneComboBox.setItemLabelGenerator(timezone -> timezone.name());
		timezoneComboBox.setRenderer(new ComponentRenderer<>(timezone -> {
			Span nameSpan = new Span(timezone.name());
			return nameSpan;
		}));
		add(nameTextField, urlTextField, latNumberField, lonNumberField, timezoneComboBox);
	}

	public CalendarSourceForm populateWith(CalendarSource calendarSource) {
		timezoneComboBox.setItems(R.timezoneRepo().findAll());

		nameTextField.setValue(calendarSource.name() == null ? "" : calendarSource.name());
		urlTextField.setValue(calendarSource.url() == null ? "" : calendarSource.url());
		latNumberField.setValue(calendarSource.lat());
		lonNumberField.setValue(calendarSource.lon());
		timezoneComboBox.setValue(calendarSource.timezone() == null ? R.timezoneRepo().findAll().get(0) : calendarSource.timezone());

		return this;
	}

	public CalendarSourceForm writeTo(CalendarSource calendarSource) {
		calendarSource.name(nameTextField.getValue());
		calendarSource.url(urlTextField.getValue());
		calendarSource.lat(latNumberField.getValue());
		calendarSource.lon(lonNumberField.getValue());
		calendarSource.timezone(timezoneComboBox.getValue());

		return this;
	}

	public static void showInsertDialog(Runnable onInsert) {
		CalendarSource calendarSource = new CalendarSource();
		CalendarSourceForm calendarSourceForm = new CalendarSourceForm().populateWith(calendarSource);
		new OkCancelDialog("Source", calendarSourceForm)
				.okLabel("Save")
				.onOk(() -> {
					calendarSourceForm.writeTo(calendarSource);
					R.calendarSource().save(calendarSource);
					onInsert.run();
				})
				.open();
	}
}