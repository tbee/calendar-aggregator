package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.domain.entity.CalendarLocation;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceAIScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarSourceAIScraperForm extends CalendarSourceScraperBaseHTMLForm {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarSourceAIScraperForm.class);

	private final Binder<CalendarSourceAIScraper> binder = new Binder<>();

	private final TextArea promptTextArea = new TextArea("Prompt");

	public CalendarSourceAIScraperForm() {

		Button testButton = new Button("Test", evt -> test());

		FormLayout formLayout = addAsFormlayoutInAccordion("Prompt", promptTextArea, testButton);
		formLayout.setColspan(promptTextArea, 2);
		formLayout.setColspan(testButton, 2);

		binder.forField(promptTextArea).bind(CalendarSourceAIScraper::prompt, CalendarSourceAIScraper::prompt);
	}

	private void test() {
		CalendarSourceAIScraper calendarSource = new CalendarSourceAIScraper();
		new CalendarLocation().enabled(true).addCalendarSource(calendarSource);
		generateAndShowTrace(calendarSource);
	}

	@Override
	public CalendarSourceAIScraperForm populateWith(CalendarSource calendarSource) {
		super.populateWith(calendarSource);
		if (calendarSource instanceof CalendarSourceAIScraper calendarSourceAIScraper) {
			binder.readBean(calendarSourceAIScraper);
		}
		return this;
	}

	@Override
	public CalendarSourceAIScraperForm writeTo(CalendarSource calendarSource) throws ValidationException {
		super.writeTo(calendarSource);
		if (calendarSource instanceof CalendarSourceAIScraper calendarSourceAIScraper) {
			binder.writeBean(calendarSourceAIScraper);
		}
		return this;
	}
}