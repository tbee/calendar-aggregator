package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.application.vdn.component.CancelDialog;
import nl.softworks.calendarAggregator.application.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceManual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class CalendarSourceForm extends FormLayout {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceForm.class);

	private final Binder<CalendarSource> binder = new Binder<>();

	private final TextField statusTextField = new TextField("Status");

	private CalendarSource calendarSource;
	public CalendarSourceForm() {
		setColspan(statusTextField, 2);
		add(statusTextField);

		Button generateButton = new Button("Generate", evt -> generate());
		setColspan(generateButton, 2);
		add(generateButton);

		binder.forField(statusTextField).bind(CalendarSource::status, CalendarSource::status);
	}

	public CalendarSourceForm populateWith(CalendarSource calendarSource) {
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
		CalendarSourceManual calendarSource = new CalendarSourceManual();
		CalendarSourceManualForm calendarSourceManualForm = new CalendarSourceManualForm().populateWith(selectedCalendarSource != null ? selectedCalendarSource : calendarSource);
		new OkCancelDialog("Manual Source", calendarSourceManualForm)
				.okLabel("Save")
				.onOk(() -> {
					try {
						calendarSourceManualForm.writeTo(calendarSource);
						R.calendarSource().save(calendarSource);
						onInsert.run();
					} catch (ValidationException e) {
						throw new RuntimeException(e);
					}
				})
				.open();
	}
}