package nl.softworks.calendarAggregator.boundary.vdn;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CalendarEventForm extends FormLayout {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarEventForm.class);
	private static final DateTimeFormatter YYYYMMDDHHMM = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final DatePicker startDatePicker = new DatePicker("Start date");
	private final TimePicker startTimePicker = new TimePicker("Start time");
	private final DatePicker endDatePicker = new DatePicker("End date");
	private final TimePicker endTimePicker = new TimePicker("End time");
	private final TextField summaryTextField = new TextField("Summary");

	public CalendarEventForm() {
		DatePicker.DatePickerI18n datePickerIsoFormat = new DatePicker.DatePickerI18n();
		datePickerIsoFormat.setDateFormat("yyyy-MM-dd");

		startDatePicker.setI18n(datePickerIsoFormat);
		endDatePicker.setI18n(datePickerIsoFormat);
		add(startDatePicker, startTimePicker, endDatePicker, endTimePicker, summaryTextField);
		setColspan(summaryTextField, 2);
	}

	public void populateWith(CalendarEvent calendarEvent) {
		LocalDateTime startDateTime = calendarEvent.startDateTime();
		LocalDateTime endDateTime = calendarEvent.endDateTime();
		String subject = calendarEvent.subject();

		startDatePicker.setValue(startDateTime == null ? null : startDateTime.toLocalDate());
		startTimePicker.setValue(startDateTime == null ? null : startDateTime.toLocalTime());
		endDatePicker.setValue(endDateTime == null ? null : endDateTime.toLocalDate());
		endTimePicker.setValue(endDateTime == null ? null : calendarEvent.endDateTime().toLocalTime());
		summaryTextField.setValue(subject);
	}
}