package nl.softworks.calendarAggregator.boundary.vdn.form;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializablePredicate;
import nl.softworks.calendarAggregator.boundary.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.domain.boundary.C;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.Timezone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.jakarta.validator.UrlValidatorImpl;

public class CalendarSourceForm extends FormLayout {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceForm.class);

	private final Binder<CalendarSource> binder = new Binder<>();

	private final TextField nameTextField = new TextField("Name");
	private final TextField urlTextField = new TextField("Url");
	private final TextField locationTextField = new TextField("Location");
	private final NumberField latNumberField = new NumberField("LAT");
	private final NumberField lonNumberField = new NumberField("LON");
	private final ComboBox<Timezone> timezoneComboBox = new ComboBox<>("Timezone");

	public CalendarSourceForm() {
		timezoneComboBox.setItemLabelGenerator(timezone -> timezone.name());
		timezoneComboBox.setRenderer(new ComponentRenderer<>(timezone -> {
			Span nameSpan = new Span(timezone.name());
			return nameSpan;
		}));
		add(nameTextField, urlTextField, locationTextField, latNumberField, lonNumberField, timezoneComboBox);

		binder.forField(nameTextField).bind(CalendarSource::name, CalendarSource::name);
		binder.forField(urlTextField).withValidator(s -> UrlValidatorImpl.isValid(s), "Illegal URL").bind(CalendarSource::url, CalendarSource::url);
		binder.forField(locationTextField).bind(CalendarSource::location, CalendarSource::location);
		binder.forField(latNumberField).bind(CalendarSource::lat, CalendarSource::lat);
		binder.forField(lonNumberField).bind(CalendarSource::lon, CalendarSource::lon);
		binder.forField(timezoneComboBox).bind(CalendarSource::timezone, CalendarSource::timezone);
	}

	public CalendarSourceForm populateWith(CalendarSource calendarSource) {
		timezoneComboBox.setItems(R.timezoneRepo().findAll());
		binder.readBean(calendarSource);
		return this;
	}

	public CalendarSourceForm writeTo(CalendarSource calendarSource) throws ValidationException {
		binder.writeBean(calendarSource);
		return this;
	}

	public static void showInsertDialog(Runnable onInsert) {
		CalendarSource calendarSource = new CalendarSource();
		CalendarSourceForm calendarSourceForm = new CalendarSourceForm().populateWith(calendarSource);
		new OkCancelDialog("Source", calendarSourceForm)
				.okLabel("Save")
				.onOk(() -> {
					try {
						calendarSourceForm.writeTo(calendarSource);
						R.calendarSource().save(calendarSource);
						onInsert.run();
					} catch (ValidationException e) {
						throw new RuntimeException(e);
					}
				})
				.open();
	}
}