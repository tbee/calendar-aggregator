package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import nl.softworks.calendarAggregator.application.vdn.component.AnchorIcon;
import nl.softworks.calendarAggregator.application.vdn.component.ResultDialog;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarLocation;
import nl.softworks.calendarAggregator.domain.entity.Timezone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.jakarta.validator.UrlValidatorImpl;
import org.tbee.webstack.vdn.component.ConfirmationDialog;

import java.util.List;
import java.util.stream.Collectors;

public class CalendarLocationForm extends FormLayout {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarLocationForm.class);

	private final Binder<CalendarLocation> binder = new Binder<>();

	private final TextField nameTextField = new TextField("Name");
	private final TextField urlTextField = new TextField("URL");
	private final TextField locationTextField = new TextField("Location");
	private final NumberField latNumberField = new NumberField("LAT");
	private final NumberField lonNumberField = new NumberField("LON");
	private final Checkbox enabledCheckbox = new Checkbox("Enabled");
	private final ComboBox<Timezone> timezoneComboBox = new ComboBox<>("Timezone");
	private final Anchor mapPinAnchor = AnchorIcon.mapPin("?");

	private CalendarLocation calendarLocation;
	public CalendarLocationForm() {

		timezoneComboBox.setItemLabelGenerator(Timezone::name);
		timezoneComboBox.setRenderer(new ComponentRenderer<>(timezone -> {
			Span nameSpan = new Span(timezone.name());
			return nameSpan;
		}));
		setColspan(urlTextField, 2);
		add(nameTextField, new HorizontalLayout(enabledCheckbox, mapPinAnchor), urlTextField, locationTextField, timezoneComboBox, latNumberField, lonNumberField);

		Button generateButton = new Button("Generate", evt -> generate());
		setColspan(generateButton, 2);
		add(generateButton);

		binder.forField(nameTextField).bind(CalendarLocation::name, CalendarLocation::name);
		binder.forField(urlTextField).withValidator(UrlValidatorImpl::isValid, "Illegal URL").bind(CalendarLocation::url, CalendarLocation::url);
		binder.forField(locationTextField).bind(CalendarLocation::location, CalendarLocation::location);
		binder.forField(latNumberField).bind(CalendarLocation::lat, CalendarLocation::lat);
		binder.forField(lonNumberField).bind(CalendarLocation::lon, CalendarLocation::lon);
		binder.forField(enabledCheckbox).bind(CalendarLocation::enabled, CalendarLocation::enabled);
		binder.forField(timezoneComboBox).bind(CalendarLocation::timezone, CalendarLocation::timezone);
		binder.addValueChangeListener(event ->  {
			if (event instanceof ComponentValueChangeEvent<?,?> componentValueChangeEvent) {
				Component source = componentValueChangeEvent.getSource();
				if (source == locationTextField || source == latNumberField || source == lonNumberField) {
					setMapPinAnchorHrefFromFormFields();
				}
			}
		});
	}

	private void setMapPinAnchorHrefFromFormFields() {
		try {
			CalendarLocation cl = new CalendarLocation();
			binder.writeBean(cl);
			mapPinAnchor.setHref(cl.determineGoogleMapURL());
		}
		catch (ValidationException e) {
			throw new RuntimeException(e);
		}
	}

	public CalendarLocationForm populateWith(CalendarLocation calendarLocation) {
		timezoneComboBox.setItems(R.timezone().findAll());
		binder.readBean(calendarLocation);
		mapPinAnchor.setHref(calendarLocation.determineGoogleMapURL());
		this.calendarLocation = calendarLocation;
		return this;
	}

	public CalendarLocationForm writeTo(CalendarLocation calendarLocation) throws ValidationException {
		binder.writeBean(calendarLocation);
		return this;
	}

	/*
	 * This uses the actual source, so it can be saved
	 */
	private void generate() {
		String calendarEventsString = "";
		try {
			writeTo(calendarLocation);
			List<CalendarEvent> calendarEvents = calendarLocation.generateEvents();
			populateWith(calendarLocation);

			calendarEventsString = calendarEvents.stream().map(s -> s.calendarSource().type() + ": " + s + "\n").collect(Collectors.joining());
			if (calendarEventsString.isBlank()) {
				calendarEventsString = "No events were generated";
			}
		}
		catch (ValidationException | RuntimeException e) {
			Notification.show(e.toString(), 5000, Notification.Position.BOTTOM_CENTER);
		}

		new ResultDialog(calendarEventsString).open();
	}

	public static void showInsertDialog(CalendarLocation selectedCalendarLocation, Runnable onInsert) {
		CalendarLocation calendarLocation = new CalendarLocation();
		CalendarLocationForm calendarLocationForm = new CalendarLocationForm().populateWith(selectedCalendarLocation != null ? selectedCalendarLocation : calendarLocation);
		ConfirmationDialog.confirmCancel("Location", calendarLocationForm)
				.confirmText("Save")
				.onConfirm(() -> {
					try {
						calendarLocationForm.writeTo(calendarLocation);
						R.calendarLocation().save(calendarLocation);
						onInsert.run();
					} catch (ValidationException e) {
						throw new RuntimeException(e);
					}
				})
				.open();
	}
}