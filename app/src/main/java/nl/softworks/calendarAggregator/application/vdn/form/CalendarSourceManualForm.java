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
import nl.softworks.calendarAggregator.application.vdn.component.CrudButtonbar;
import nl.softworks.calendarAggregator.application.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceManual;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceManualExdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CalendarSourceManualForm extends CalendarSourceForm {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarSourceManualForm.class);

	private final Binder<CalendarSourceManual> binder = new Binder<>();

	private final DateTimePicker startDateTimePicker = new DateTimePicker("Start date");
	private final DateTimePicker endDateTimePicker = new DateTimePicker("End date");
	private final TextField rruleTextField = new TextField("RRule");
	private final MultiSelectListBox<CalendarSourceManualExdate> calendarEventExdateListBox = new MultiSelectListBox<>();
	protected final List<CalendarSourceManualExdate> calendarSourceManualExdates = new ArrayList<>();
	private final TextField subjectTextField = new TextField("Subject");
	private final DatePicker.DatePickerI18n datePickerIsoFormat = new DatePicker.DatePickerI18n();
	private final Anchor rruleHelpAnchor = new Anchor("https://freetools.textmagic.com/rrule-generator", "RRule builder", AnchorTarget.BLANK);
	private final CrudButtonbar crudButtonbar = new CrudButtonbar()
			.onInsert(this::insertExdate)
			.onEdit(this::editExdate)
			.onDelete(this::deleteExdate);

	public CalendarSourceManualForm() {
		datePickerIsoFormat.setDateFormat("yyyy-MM-dd");
		startDateTimePicker.setDatePickerI18n(datePickerIsoFormat);
		endDateTimePicker.setDatePickerI18n(datePickerIsoFormat);

		calendarEventExdateListBox.setRenderer(new ComponentRenderer<>(cee -> {
			Span excludedDateSpan = new Span(cee.excludedDate().toString());
			return excludedDateSpan;
		}));

		setColspan(subjectTextField, 2);

		FormLayout formLayout = addAsFormlayoutInAccordion("Manual", subjectTextField, startDateTimePicker, endDateTimePicker, rruleTextField, rruleHelpAnchor);
		HorizontalLayout exdateGroup = new HorizontalLayout(calendarEventExdateListBox, crudButtonbar);
		formLayout.addFormItem(exdateGroup, "Exdates");

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
		Set<CalendarSourceManualExdate> selectedItems = calendarEventExdateListBox.getSelectedItems();
		if (selectedItems.isEmpty()) {
			return;
		}
		new OkCancelDialog("Remove", new NativeLabel("Remove " + selectedItems.size() + " date(s). Are you sure?"))
				.okLabel("Remove")
				.onOk(() -> {
					calendarSourceManualExdates.removeAll(selectedItems);
					calendarEventExdateListBox.setItems(calendarSourceManualExdates);
				})
				.open();
	}

	private void editExdate() {
		Set<CalendarSourceManualExdate> selectedItems = calendarEventExdateListBox.getSelectedItems();
		if (selectedItems.isEmpty()) {
			return;
		}
		CalendarSourceManualExdate calendarSourceManualExdate = selectedItems.iterator().next();
		DatePicker datePicker = new DatePicker(calendarSourceManualExdate.excludedDate());
		datePicker.setI18n(datePickerIsoFormat);
		new OkCancelDialog("Modify", datePicker)
				.okLabel("Modify")
				.onOk(() -> {
					LocalDate localDate = datePicker.getValue();
					calendarSourceManualExdate.excludedDate(localDate);
					calendarEventExdateListBox.setItems(calendarSourceManualExdates);
				})
				.open();
	}

	private void insertExdate() {
		DatePicker datePicker = new DatePicker();
		datePicker.setI18n(datePickerIsoFormat);
		new OkCancelDialog("Add", datePicker)
				.okLabel("Add")
				.onOk(() -> {
					LocalDate localDate = datePicker.getValue();
					calendarSourceManualExdates.add(new CalendarSourceManualExdate().excludedDate(localDate));
					calendarEventExdateListBox.setItems(calendarSourceManualExdates);
				})
				.open();
	}

	public CalendarSourceManualForm populateWith(CalendarSource calendarSource) {
		super.populateWith(calendarSource);
		if (calendarSource instanceof CalendarSourceManual calendarSourceManual) {
			binder.readBean(calendarSourceManual);
			calendarSourceManualExdates.clear();
			calendarSourceManualExdates.addAll(calendarSource == null ? List.of() : calendarSourceManual.exdates());
			calendarEventExdateListBox.setItems(calendarSourceManualExdates);
		}
		return this;
	}

	@Override
	public CalendarSourceManualForm writeTo(CalendarSource calendarSource) throws ValidationException {
		super.writeTo(calendarSource);
		if (calendarSource instanceof CalendarSourceManual calendarSourceManual) {
			binder.writeBean(calendarSourceManual);
			calendarSourceManual.exdates(calendarSourceManualExdates);
		}
		return this;
	}
}