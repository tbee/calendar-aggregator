package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.domain.entity.CalendarLocation;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceICal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarSourceICalForm extends CalendarSourceForm {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarSourceICalForm.class);

	private final Binder<CalendarSourceICal> binder = new Binder<>();

	private final TextField icalUrlTextfield = new TextField("ICal URL");
	private final TextField regexTextField = new TextField("Regex");

	public CalendarSourceICalForm() {
		icalUrlTextfield.setTooltipText("Use `=@{now.plusYears(1).format(yyyy_MM_dd)}` to insert code.");

		Button testButton = new Button("Test", evt -> test());

		FormLayout formLayout = addAsFormlayoutInAccordion("ICal", icalUrlTextfield, regexTextField, testButton);
		formLayout.setColspan(icalUrlTextfield, 2);
		formLayout.setColspan(regexTextField, 2);
		formLayout.setColspan(testButton, 2);

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
		if (calendarSource instanceof CalendarSourceICal calendarSourceICal) {
			binder.readBean(calendarSourceICal);
		}
		return this;
	}

	@Override
	public CalendarSourceICalForm writeTo(CalendarSource calendarSource) throws ValidationException {
		super.writeTo(calendarSource);
		if (calendarSource instanceof CalendarSourceICal calendarSourceICal) {
			binder.writeBean(calendarSourceICal);
		}
		return this;
	}
}