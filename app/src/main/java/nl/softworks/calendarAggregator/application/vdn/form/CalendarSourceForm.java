package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.application.vdn.component.CancelDialog;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

abstract public class CalendarSourceForm extends FormLayout {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceForm.class);

	private final Binder<CalendarSource> binder = new Binder<>();

	private final TextField descriptionTextfield = new TextField("Description");
	private final TextField statusTextField = new TextField("Status");

	private CalendarSource calendarSource;
	public CalendarSourceForm() {
		setColspan(descriptionTextfield, 2);
		setColspan(statusTextField, 2);
		add(descriptionTextfield, statusTextField);

		Button generateButton = new Button("Generate", evt -> generate());
		setColspan(generateButton, 2);
		add(generateButton);

		binder.forField(descriptionTextfield).bind(CalendarSource::description, CalendarSource::description);
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
		try {
			writeTo(calendarSource);
			List<CalendarEvent> calendarEvents = calendarSource.generateEvents();
			populateWith(calendarSource);

			String calendarEventsString = calendarEvents.stream().map(s -> s + "\n").collect(Collectors.joining());
			calendarSource.logAppend("\n\n" + calendarEventsString);
		}
		catch (ValidationException | RuntimeException e) {
			StringWriter stringWriter = new StringWriter();
			e.printStackTrace(new PrintWriter(stringWriter));
			calendarSource.logAppend(stringWriter.toString());
		}

		TextArea textArea = new TextArea("Result", calendarSource.log(), "");
		textArea.setSizeFull();

		CancelDialog cancelDialog = new CancelDialog("Result", textArea);
		cancelDialog.setSizeFull();
		cancelDialog.open();
	}
}