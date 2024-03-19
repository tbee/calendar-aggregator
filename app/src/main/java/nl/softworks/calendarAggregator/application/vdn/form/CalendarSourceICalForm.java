package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.application.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarLocation;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceICal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarSourceICalForm extends CalendarSourceForm {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceICalForm.class);

	private final Binder<CalendarSourceICal> binder = new Binder<>();

	private final TextField icalUrlTextfield = new TextField("ICal URL");
	private final TextField regexTextField = new TextField("Regex");

	public CalendarSourceICalForm() {
		setColspan(icalUrlTextfield, 2);
		setColspan(regexTextField, 2);

		Button testButton = new Button("Test", evt -> test());
		setColspan(testButton, 2);
		add(icalUrlTextfield, regexTextField, testButton);

		binder.forField(icalUrlTextfield).bind(CalendarSourceICal::icalUrl, CalendarSourceICal::icalUrl);
		binder.forField(regexTextField).bind(CalendarSourceICal::regex, CalendarSourceICal::regex);
	}

	private void test() {
		CalendarSourceICal calendarSource = new CalendarSourceICal();
		new CalendarLocation().enabled(true).addCalendarSource(calendarSource);
		generateAndShowTrace(calendarSource);
	}

	@Override
	public CalendarSourceICalForm populateWith(CalendarSource calendarSource) {
		super.populateWith(calendarSource);
		binder.readBean((CalendarSourceICal)calendarSource);
		return this;
	}

	@Override
	public CalendarSourceICalForm writeTo(CalendarSource calendarSource) throws ValidationException {
		super.writeTo(calendarSource);
		binder.writeBean((CalendarSourceICal)calendarSource);
		return this;
	}

	public static void showInsertDialog(CalendarSource selectedCalendarSource, Runnable onInsert) {
		CalendarSourceICal calendarSource = new CalendarSourceICal();
		CalendarSourceICalForm calendarSourceForm = new CalendarSourceICalForm().populateWith(selectedCalendarSource != null ? selectedCalendarSource : calendarSource);
		new OkCancelDialog("Source", calendarSourceForm)
				.okLabel("Save")
				.onOk(() -> {
					try {
						calendarSourceForm.writeTo(calendarSource);
						R.calendarSource().save(calendarSource);
						onInsert.run();
					} catch (ValidationException e) {
						throw new RuntimeException(e);
					}
				})
				.open();
	}
}