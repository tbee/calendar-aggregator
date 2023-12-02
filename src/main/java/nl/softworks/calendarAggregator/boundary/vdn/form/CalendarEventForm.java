package nl.softworks.calendarAggregator.boundary.vdn.form;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.boundary.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarEventForm extends FormLayout {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarEventForm.class);

	private final Binder<CalendarEvent> binder = new Binder<>();

	private final DateTimePicker startDateTimePicker = new DateTimePicker("Start date");
	private final DateTimePicker endDateTimePicker = new DateTimePicker("End date");
	private final TextField rruleTextField = new TextField("RRule");
	private final TextField subjectTextField = new TextField("Summary");

	public CalendarEventForm() {
		DatePicker.DatePickerI18n datePickerIsoFormat = new DatePicker.DatePickerI18n();
		datePickerIsoFormat.setDateFormat("yyyy-MM-dd");
		startDateTimePicker.setDatePickerI18n(datePickerIsoFormat);
		endDateTimePicker.setDatePickerI18n(datePickerIsoFormat);

		Anchor rruleHelpAnchor = new Anchor("https://freetools.textmagic.com/rrule-generator", "RRule builder", AnchorTarget.BLANK);
		add(startDateTimePicker, endDateTimePicker, rruleTextField, rruleHelpAnchor, subjectTextField);
		setColspan(subjectTextField, 2);

		binder.forField(startDateTimePicker).bind(CalendarEvent::startDateTime, CalendarEvent::startDateTime);
		binder.forField(endDateTimePicker).bind(CalendarEvent::endDateTime, CalendarEvent::endDateTime);
		binder.forField(rruleTextField).bind(CalendarEvent::rrule, CalendarEvent::rrule);
		binder.forField(subjectTextField).bind(CalendarEvent::subject, CalendarEvent::subject);
	}

	public CalendarEventForm populateWith(CalendarEvent calendarEvent) {
		binder.readBean(calendarEvent);
		return this;
	}

	public CalendarEventForm writeTo(CalendarEvent calendarEvent) throws ValidationException {
		binder.writeBean(calendarEvent);
		return this;
	}

	public static void showInsertDialog(CalendarSource calendarSource, Runnable onInsert) {
		CalendarEvent calendarEvent = new CalendarEvent().generated(false);
		CalendarEventForm calendarEventForm = new CalendarEventForm().populateWith(calendarEvent);
		new OkCancelDialog("Event", calendarEventForm)
				.okLabel("Save")
				.onOk(() -> {
					try {
						calendarEventForm.writeTo(calendarEvent);
						calendarSource.addCalendarEvent(calendarEvent);
						R.calendarEvent().save(calendarEvent);
						onInsert.run();
					} catch (ValidationException e) {
						throw new RuntimeException(e);
					}
				})
				.open();
	}
}