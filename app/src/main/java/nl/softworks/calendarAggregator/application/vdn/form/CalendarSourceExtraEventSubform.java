package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceExtraEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarSourceExtraEventSubform extends FormLayout  {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarSourceExtraEventSubform.class);

	private final Binder<CalendarSourceExtraEvent> binder = new Binder<>();

	private final DateTimePicker startDateTimePicker = new DateTimePicker();
	private final DateTimePicker endDateTimePicker = new DateTimePicker();
	private final TextField subjectTextField = new TextField();
	private final DatePicker.DatePickerI18n datePickerIsoFormat = new DatePicker.DatePickerI18n();

	public CalendarSourceExtraEventSubform() {
		datePickerIsoFormat.setDateFormat("yyyy-MM-dd");
		startDateTimePicker.setDatePickerI18n(datePickerIsoFormat);
		endDateTimePicker.setDatePickerI18n(datePickerIsoFormat);

		setResponsiveSteps(new ResponsiveStep("0", 1));
		addFormItem(startDateTimePicker, "Start datetime");
		addFormItem(endDateTimePicker, "End datetime");
		addFormItem(subjectTextField, "Subject");
		subjectTextField.setWidthFull();

		binder.forField(startDateTimePicker).bind(CalendarSourceExtraEvent::startDateTime, CalendarSourceExtraEvent::startDateTime);
		binder.forField(endDateTimePicker).bind(CalendarSourceExtraEvent::endDateTime, CalendarSourceExtraEvent::endDateTime);
		binder.forField(subjectTextField).bind(CalendarSourceExtraEvent::subject, CalendarSourceExtraEvent::subject);

		startDateTimePicker.addValueChangeListener(event -> {
			if (startDateTimePicker.getValue() != null && endDateTimePicker.isEmpty()) {
				endDateTimePicker.setValue(startDateTimePicker.getValue().plusHours(2));
			}
        });
	}

	public CalendarSourceExtraEventSubform populateWith(CalendarSourceExtraEvent calendarSourceExtraEvent) {
		binder.readBean(calendarSourceExtraEvent);
		return this;
	}

	public CalendarSourceExtraEventSubform writeTo(CalendarSourceExtraEvent calendarSourceExtraEvent) throws ValidationException {
		binder.writeBean(calendarSourceExtraEvent);
		return this;
	}
}