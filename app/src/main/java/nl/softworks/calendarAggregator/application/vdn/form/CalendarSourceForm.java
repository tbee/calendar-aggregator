package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import nl.softworks.calendarAggregator.application.vdn.component.CancelDialog;
import nl.softworks.calendarAggregator.application.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.Timezone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.jakarta.validator.UrlValidatorImpl;

import java.util.List;
import java.util.stream.Collectors;

public class CalendarSourceForm extends FormLayout {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceForm.class);

	private final Binder<CalendarSource> binder = new Binder<>();

	private final TextField nameTextField = new TextField("Name");
	private final TextField urlTextField = new TextField("Url");
	private final TextField locationTextField = new TextField("Location");
	private final NumberField latNumberField = new NumberField("LAT");
	private final NumberField lonNumberField = new NumberField("LON");
	private final Checkbox enabledCheckbox = new Checkbox("Enabled");
	private final ComboBox<Timezone> timezoneComboBox = new ComboBox<>("Timezone");
	private final TextField statusTextField = new TextField("Status");

	private CalendarSource calendarSource;
	public CalendarSourceForm() {
		timezoneComboBox.setItemLabelGenerator(timezone -> timezone.name());
		timezoneComboBox.setRenderer(new ComponentRenderer<>(timezone -> {
			Span nameSpan = new Span(timezone.name());
			return nameSpan;
		}));
		setColspan(urlTextField, 2);
		setColspan(statusTextField, 2);
		add(nameTextField, enabledCheckbox, urlTextField, locationTextField, timezoneComboBox, latNumberField, lonNumberField, statusTextField);

		Button generateButton = new Button("Generate", evt -> generate());
		setColspan(generateButton, 2);
		add(generateButton);

		binder.forField(nameTextField).bind(CalendarSource::name, CalendarSource::name);
		binder.forField(urlTextField).withValidator(s -> UrlValidatorImpl.isValid(s), "Illegal URL").bind(CalendarSource::url, CalendarSource::url);
		binder.forField(locationTextField).bind(CalendarSource::location, CalendarSource::location);
		binder.forField(latNumberField).bind(CalendarSource::lat, CalendarSource::lat);
		binder.forField(lonNumberField).bind(CalendarSource::lon, CalendarSource::lon);
		binder.forField(enabledCheckbox).bind(CalendarSource::enabled, CalendarSource::enabled);
		binder.forField(timezoneComboBox).bind(CalendarSource::timezone, CalendarSource::timezone);
		binder.forField(statusTextField).bind(CalendarSource::status, CalendarSource::status);
	}

	public CalendarSourceForm populateWith(CalendarSource calendarSource) {
		timezoneComboBox.setItems(R.timezone().findAll());
		binder.readBean(calendarSource);
		this.calendarSource = calendarSource;
		return this;
	}

	public CalendarSourceForm writeTo(CalendarSource calendarSource) throws ValidationException {
		binder.writeBean(calendarSource);
		return this;
	}

	/*
	 * This uses the actual source, so it can be saved
	 */
	private void generate() {
		generateAndShowTrace(calendarSource);
	}

	protected void generateAndShowTrace(CalendarSource calendarSource) {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			writeTo(calendarSource);
			List<CalendarEvent> calendarEvents = calendarSource.generateEvents(stringBuilder);
			calendarSource.log(stringBuilder.toString());
			populateWith(calendarSource);

			String calendarEventsString = calendarEvents.stream().map(s -> s + "\n").collect(Collectors.joining());
			stringBuilder.append("\n\n").append(calendarEventsString);
		}
		catch (ValidationException | RuntimeException e) {
			calendarSource.log(e.toString());
			Notification.show(e.toString(), 5000, Notification.Position.BOTTOM_CENTER);
		}

		TextArea textArea = new TextArea("Result", stringBuilder.toString(), "");
		textArea.setSizeFull();

		CancelDialog cancelDialog = new CancelDialog("Result", textArea);
		cancelDialog.setSizeFull();
		cancelDialog.open();
	}

	public static void showInsertDialog(CalendarSource selectedCalendarSource, Runnable onInsert) {
		CalendarSource calendarSource = new CalendarSource();
		CalendarSourceForm calendarSourceForm = new CalendarSourceForm().populateWith(selectedCalendarSource != null ? selectedCalendarSource : calendarSource);
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