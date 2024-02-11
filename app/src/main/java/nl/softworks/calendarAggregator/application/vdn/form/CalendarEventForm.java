package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.formlayout.FormLayout;

public class CalendarEventForm extends FormLayout {
//	private static final Logger LOG = LoggerFactory.getLogger(CalendarEventForm.class);
//
//	private final Binder<CalendarEvent> binder = new Binder<>();
//
//	private final DateTimePicker startDateTimePicker = new DateTimePicker("Start date");
//	private final DateTimePicker endDateTimePicker = new DateTimePicker("End date");
//	private final MultiSelectListBox<CalendarEventExdate> calendarEventExdateListBox = new MultiSelectListBox<>();
//	protected final List<CalendarEventExdate> calendarEventExdates = new ArrayList<>();
//	private final TextField subjectTextField = new TextField("Summary");
//	private final DatePicker.DatePickerI18n datePickerIsoFormat = new DatePicker.DatePickerI18n();
//	private final Anchor rruleHelpAnchor = new Anchor("https://freetools.textmagic.com/rrule-generator", "RRule builder", AnchorTarget.BLANK);
//	private final CrudButtonbar crudButtonbar = new CrudButtonbar()
//			.onInsert(this::insertExdate)
//			.onEdit(this::editExdate)
//			.onDelete(this::deleteExdate);
//
//	public CalendarEventForm() {
//		datePickerIsoFormat.setDateFormat("yyyy-MM-dd");
//		startDateTimePicker.setDatePickerI18n(datePickerIsoFormat);
//		endDateTimePicker.setDatePickerI18n(datePickerIsoFormat);
//
//		calendarEventExdateListBox.setRenderer(new ComponentRenderer<>(cee -> {
//			Span excludedDateSpan = new Span(cee.excludedDate().toString());
//			return excludedDateSpan;
//		}));
//
//		add(startDateTimePicker, endDateTimePicker, rruleHelpAnchor);
//		HorizontalLayout exdateGroup = new HorizontalLayout(calendarEventExdateListBox, crudButtonbar);
//		addFormItem(exdateGroup, "Exdates");
//		add(subjectTextField, 2);
//
//		binder.forField(startDateTimePicker).bind(CalendarEvent::startDateTime, CalendarEvent::startDateTime);
//		binder.forField(endDateTimePicker).bind(CalendarEvent::endDateTime, CalendarEvent::endDateTime);
//		binder.forField(subjectTextField).bind(CalendarEvent::subject, CalendarEvent::subject);
//
//		startDateTimePicker.addValueChangeListener(event -> {
//			if (endDateTimePicker.isEmpty()) {
//				endDateTimePicker.setValue(startDateTimePicker.getValue());
//			}
//        });
//	}
//
//	private void deleteExdate() {
//		Set<CalendarEventExdate> selectedItems = calendarEventExdateListBox.getSelectedItems();
//		if (selectedItems.isEmpty()) {
//			return;
//		}
//		new OkCancelDialog("Remove", new NativeLabel("Remove " + selectedItems.size() + " date(s). Are you sure?"))
//				.okLabel("Remove")
//				.onOk(() -> {
//					calendarEventExdates.removeAll(selectedItems);
//					calendarEventExdateListBox.setItems(calendarEventExdates);
//				})
//				.open();
//	}
//
//	private void editExdate() {
//		Set<CalendarEventExdate> selectedItems = calendarEventExdateListBox.getSelectedItems();
//		if (selectedItems.isEmpty()) {
//			return;
//		}
//		CalendarEventExdate calendarEventExdate = selectedItems.iterator().next();
//		DatePicker datePicker = new DatePicker(calendarEventExdate.excludedDate());
//		datePicker.setI18n(datePickerIsoFormat);
//		new OkCancelDialog("Modify", datePicker)
//				.okLabel("Modify")
//				.onOk(() -> {
//					LocalDate localDate = datePicker.getValue();
//					calendarEventExdate.excludedDate(localDate);
//					calendarEventExdateListBox.setItems(calendarEventExdates);
//				})
//				.open();
//	}
//
//	private void insertExdate() {
//		DatePicker datePicker = new DatePicker();
//		datePicker.setI18n(datePickerIsoFormat);
//		new OkCancelDialog("Add", datePicker)
//				.okLabel("Add")
//				.onOk(() -> {
//					LocalDate localDate = datePicker.getValue();
//					calendarEventExdates.add(new CalendarEventExdate().excludedDate(localDate));
//					calendarEventExdateListBox.setItems(calendarEventExdates);
//				})
//				.open();
//	}
//
//	public CalendarEventForm populateWith(CalendarEvent calendarEvent) {
//		binder.readBean(calendarEvent);
//		calendarEventExdates.clear();
//		calendarEventExdates.addAll(calendarEvent == null ? List.of() : calendarEvent.calendarEventExdates());
//		calendarEventExdateListBox.setItems(calendarEventExdates);
//		return this;
//	}
//
//	public CalendarEventForm writeTo(CalendarEvent calendarEvent) throws ValidationException {
//		binder.writeBean(calendarEvent);
//		calendarEvent.calendarEventExdates(calendarEventExdates);
//		return this;
//	}
//
//	public static void showInsertDialog(CalendarSource calendarSource, Runnable onInsert) {
//		CalendarEvent calendarEvent = new CalendarEvent();
//		CalendarEventForm calendarEventForm = new CalendarEventForm().populateWith(calendarEvent);
//		new OkCancelDialog("Event", calendarEventForm)
//				.okLabel("Save")
//				.onOk(() -> {
//					try {
//						calendarEventForm.writeTo(calendarEvent);
//						calendarSource.addCalendarEvent(calendarEvent);
//						R.calendarSource().save(calendarSource);
//						onInsert.run();
//					} catch (ValidationException e) {
//						throw new RuntimeException(e);
//					}
//				})
//				.open();
//	}
}