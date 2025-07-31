package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceExtraEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceManual;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceManualExdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.webstack.vdn.component.ConfirmationDialog;
import org.tbee.webstack.vdn.component.CrudButtonbar;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CalendarSourceManualForm extends CalendarSourceForm {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarSourceManualForm.class);

	private final Binder<CalendarSourceManual> binder = new Binder<>();

	private final DateTimePicker startDateTimePicker = new DateTimePicker("Start datetime");
	private final DateTimePicker endDateTimePicker = new DateTimePicker("End datetime");
	private final TextField rruleTextField = new TextField("RRule");
	private final MultiSelectListBox<CalendarSourceManualExdate> calendarSourceManualExdatesListBox = new MultiSelectListBox<>();
	private final MultiSelectListBox<CalendarSourceExtraEvent> calendarSourceExtraEventListBox = new MultiSelectListBox<>();
	protected final List<CalendarSourceManualExdate> calendarSourceManualExdates = new ArrayList<>();
	protected final List<CalendarSourceExtraEvent> calendarSourceExtraEvents = new ArrayList<>();
	private final TextField subjectTextField = new TextField("Subject");
	private final DatePicker.DatePickerI18n datePickerIsoFormat = new DatePicker.DatePickerI18n();
	private final DateTimePicker.DateTimePickerI18n datetimePickerIsoFormat = new DateTimePicker.DateTimePickerI18n();
	private final Anchor rruleHelpAnchor = new Anchor("https://freetools.textmagic.com/rrule-generator", "RRule builder", AnchorTarget.BLANK);
	private final CrudButtonbar exdateCrudButtonbar = new CrudButtonbar()
			.onInsert(this::insertExdate)
			.onEdit(this::editExdate)
			.onDelete(this::deleteExdate);
	private final CrudButtonbar extraEventCrudButtonbar = new CrudButtonbar()
			.onInsert(this::insertExtraEvent)
			.onEdit(this::editExtraEvent)
			.onDelete(this::deleteExtraEvent);

	public CalendarSourceManualForm() {
		datePickerIsoFormat.setDateFormat("yyyy-MM-dd");
		startDateTimePicker.setDatePickerI18n(datePickerIsoFormat);
		endDateTimePicker.setDatePickerI18n(datePickerIsoFormat);

		FormLayout formLayout = addAsFormlayoutInAccordion("Manual", subjectTextField, startDateTimePicker, endDateTimePicker, rruleTextField, rruleHelpAnchor);
		formLayout.setColspan(subjectTextField, 2);

		calendarSourceManualExdatesListBox.setRenderer(new ComponentRenderer<>(cee -> new Span(cee.excludedDate().toString())));
		formLayout.addFormItem(new HorizontalLayout(calendarSourceManualExdatesListBox, exdateCrudButtonbar), "Exdates");
		calendarSourceExtraEventListBox.setRenderer(new ComponentRenderer<>(cei -> new Span(cei.startDateTime() + " - " + cei.endDateTime() + " " + cei.subject())));
		formLayout.addFormItem(new HorizontalLayout(calendarSourceExtraEventListBox, extraEventCrudButtonbar), "Extra events");

		binder.forField(startDateTimePicker).bind(CalendarSourceManual::startDateTime, CalendarSourceManual::startDateTime);
		binder.forField(endDateTimePicker).bind(CalendarSourceManual::endDateTime, CalendarSourceManual::endDateTime);
		binder.forField(rruleTextField).bind(CalendarSourceManual::rrule, CalendarSourceManual::rrule);
		binder.forField(subjectTextField).bind(CalendarSourceManual::subject, CalendarSourceManual::subject);

		startDateTimePicker.addValueChangeListener(event -> {
			if (startDateTimePicker.getValue() != null && endDateTimePicker.isEmpty()) {
				endDateTimePicker.setValue(startDateTimePicker.getValue().plusHours(2));
			}
        });
	}

	private void deleteExdate() {
		Set<CalendarSourceManualExdate> selectedItems = calendarSourceManualExdatesListBox.getSelectedItems();
		if (selectedItems.isEmpty()) {
			return;
		}
		ConfirmationDialog.confirmCancel("Remove", new NativeLabel("Remove " + selectedItems.size() + " date(s). Are you sure?"))
				.confirmText("Remove")
				.onConfirm(() -> {
					calendarSourceManualExdates.removeAll(selectedItems);
					calendarSourceManualExdatesListBox.setItems(calendarSourceManualExdates);
				})
				.open();
	}

	private void editExdate() {
		Set<CalendarSourceManualExdate> selectedItems = calendarSourceManualExdatesListBox.getSelectedItems();
		if (selectedItems.isEmpty()) {
			return;
		}
		CalendarSourceManualExdate calendarSourceManualExdate = selectedItems.iterator().next();
		DatePicker datePicker = new DatePicker(calendarSourceManualExdate.excludedDate());
		datePicker.setI18n(datePickerIsoFormat);
		ConfirmationDialog.confirmCancel("Modify", datePicker)
				.confirmText("Modify")
				.onConfirm(() -> {
					LocalDate localDate = datePicker.getValue();
					calendarSourceManualExdate.excludedDate(localDate);
					calendarSourceManualExdatesListBox.setItems(calendarSourceManualExdates);
				})
				.open();
	}

	private void insertExdate() {
		DatePicker datePicker = new DatePicker();
		datePicker.setI18n(datePickerIsoFormat);
		ConfirmationDialog.confirmCancel("Add", datePicker)
				.confirmText("Add")
				.onConfirm(() -> {
					LocalDate localDate = datePicker.getValue();
					calendarSourceManualExdates.add(new CalendarSourceManualExdate().excludedDate(localDate));
					calendarSourceManualExdatesListBox.setItems(calendarSourceManualExdates);
				})
				.open();
	}

	private void deleteExtraEvent() {
		Set<CalendarSourceExtraEvent> selectedItems = calendarSourceExtraEventListBox.getSelectedItems();
		if (selectedItems.isEmpty()) {
			return;
		}
		ConfirmationDialog.confirmCancel("Remove", new NativeLabel("Remove " + selectedItems.size() + " extra event(s). Are you sure?"))
				.confirmText("Remove")
				.onConfirm(() -> {
					calendarSourceExtraEvents.removeAll(selectedItems);
					calendarSourceExtraEventListBox.setItems(calendarSourceExtraEvents);
				})
				.open();
	}

	private void editExtraEvent() {
		Set<CalendarSourceExtraEvent> selectedItems = calendarSourceExtraEventListBox.getSelectedItems();
		if (selectedItems.isEmpty()) {
			return;
		}
		CalendarSourceExtraEvent calendarSourceExtraEvent = selectedItems.iterator().next();
		extraEventPopup("Modify", calendarSourceExtraEvent);
	}

	private void insertExtraEvent() {
		extraEventPopup("Add", new CalendarSourceExtraEvent());
	}

	private void extraEventPopup(String actionName, CalendarSourceExtraEvent calendarSourceExtraEvent) {
		CalendarSourceExtraEventSubform calendarSourceExtraEventSubform = new CalendarSourceExtraEventSubform();
		calendarSourceExtraEventSubform.populateWith(calendarSourceExtraEvent);
		ConfirmationDialog.confirmCancel(actionName, calendarSourceExtraEventSubform)
				.confirmText(actionName)
				.onConfirm(() -> {
					try {
						calendarSourceExtraEventSubform.writeTo(calendarSourceExtraEvent);

						calendarSourceExtraEvents.remove(calendarSourceExtraEvent);
						calendarSourceExtraEvents.add(calendarSourceExtraEvent);
						calendarSourceExtraEventListBox.setItems(calendarSourceExtraEvents);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
				.open();
	}


	public CalendarSourceManualForm populateWith(CalendarSource calendarSource) {
		super.populateWith(calendarSource);
		if (calendarSource instanceof CalendarSourceManual calendarSourceManual) {
			binder.readBean(calendarSourceManual);
			calendarSourceManualExdates.clear();
			calendarSourceManualExdates.addAll(calendarSource == null ? List.of() : calendarSourceManual.exdates());
			calendarSourceManualExdatesListBox.setItems(calendarSourceManualExdates);
			calendarSourceExtraEvents.clear();
			calendarSourceExtraEvents.addAll(calendarSource == null ? List.of() : calendarSourceManual.extraEvents());
			calendarSourceExtraEventListBox.setItems(calendarSourceExtraEvents);
		}
		return this;
	}

	@Override
	public CalendarSourceManualForm writeTo(CalendarSource calendarSource) throws ValidationException {
		super.writeTo(calendarSource);
		if (calendarSource instanceof CalendarSourceManual calendarSourceManual) {
			binder.writeBean(calendarSourceManual);
			calendarSourceManual.exdates(calendarSourceManualExdates);
			calendarSourceManual.extraEvents(calendarSourceExtraEvents);
		}
		return this;
	}
}