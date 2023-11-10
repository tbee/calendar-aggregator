package nl.softworks.calendarAggregator.boundary.vdn;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CalendarEventForm extends FormLayout {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarEventForm.class);
	private static final DateTimeFormatter YYYYMMDDHHMM = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final DatePicker startDatePicker = new DatePicker("Start date");
	private final TimePicker startTimePicker = new TimePicker("Start time");
	private final DatePicker endDatePicker = new DatePicker("End date");
	private final TimePicker endTimePicker = new TimePicker("End time");
	private final TextField subjectTextField = new TextField("Summary");

	public CalendarEventForm() {
		DatePicker.DatePickerI18n datePickerIsoFormat = new DatePicker.DatePickerI18n();
		datePickerIsoFormat.setDateFormat("yyyy-MM-dd");

		startDatePicker.setI18n(datePickerIsoFormat);
		endDatePicker.setI18n(datePickerIsoFormat);
		add(startDatePicker, startTimePicker, endDatePicker, endTimePicker, subjectTextField);
		setColspan(subjectTextField, 2);
	}

	public CalendarEventForm populateWith(CalendarEvent calendarEvent) {
		LocalDateTime startDateTime = calendarEvent.startDateTime();
		LocalDateTime endDateTime = calendarEvent.endDateTime();
		String subject = calendarEvent.subject();

		startDatePicker.setValue(startDateTime == null ? null : startDateTime.toLocalDate());
		startTimePicker.setValue(startDateTime == null ? null : startDateTime.toLocalTime());
		endDatePicker.setValue(endDateTime == null ? null : endDateTime.toLocalDate());
		endTimePicker.setValue(endDateTime == null ? null : calendarEvent.endDateTime().toLocalTime());
		subjectTextField.setValue(subject);

		return this;
	}

	public CalendarEventForm writeTo(CalendarEvent calendarEvent) {
		LocalDateTime startDateTime = LocalDateTime.of(startDatePicker.getValue(), startTimePicker.getValue());
		LocalDateTime endDateTime = LocalDateTime.of(endDatePicker.getValue(), endTimePicker.getValue());

		calendarEvent.startDateTime(startDateTime)
					.endDateTime(endDateTime)
					.subject(subjectTextField.getValue());

		return this;
	}
}