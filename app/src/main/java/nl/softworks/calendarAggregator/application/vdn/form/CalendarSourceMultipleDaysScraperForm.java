package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.domain.entity.CalendarLocation;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceMultipleDaysScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarSourceMultipleDaysScraperForm extends CalendarSourceScraperBaseHTMLForm {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarSourceMultipleDaysScraperForm.class);

	private final Binder<CalendarSourceMultipleDaysScraper> binder = new Binder<>();

	private final TextField regexTextField = new TextField("Regex");
	private final IntegerField startDateGroupIdxIntegerField = new IntegerField("Start date group index");
	private final IntegerField endDateGroupIdxIntegerField = new IntegerField("End date group index");
	private final TextField datePatternTextField = new TextField("Date pattern");
	private final Checkbox nearestYearCheckbox = new Checkbox("Use Nearest year");
	private final TextField startTimeDefaultTextField = new TextField("Start time default");
	private final TextField endTimeDefaultTextField = new TextField("End time default");
	private final TextField timePatternTextField = new TextField("Time pattern");
	private final TextField dateTimeLocaleTextField = new TextField("Date Time Locale");


	public CalendarSourceMultipleDaysScraperForm() {

		Button testButton = new Button("Test", evt -> testRegex());

		FormLayout formLayout = addAsFormlayoutInAccordion("Multiple days", regexTextField, testButton, startDateGroupIdxIntegerField, endDateGroupIdxIntegerField, datePatternTextField, nearestYearCheckbox, startTimeDefaultTextField, endTimeDefaultTextField, timePatternTextField, dateTimeLocaleTextField);
		formLayout.setColspan(regexTextField, 2);
		formLayout.setColspan(testButton, 2);

		binder.forField(regexTextField).bind(CalendarSourceMultipleDaysScraper::regex, CalendarSourceMultipleDaysScraper::regex);
		binder.forField(datePatternTextField).bind(CalendarSourceMultipleDaysScraper::datePattern, CalendarSourceMultipleDaysScraper::datePattern);
		binder.forField(nearestYearCheckbox).bind(CalendarSourceMultipleDaysScraper::nearestYear, CalendarSourceMultipleDaysScraper::nearestYear);
		binder.forField(startTimeDefaultTextField).bind(CalendarSourceMultipleDaysScraper::startTimeDefault, CalendarSourceMultipleDaysScraper::startTimeDefault);
		binder.forField(endTimeDefaultTextField).bind(CalendarSourceMultipleDaysScraper::endTimeDefault, CalendarSourceMultipleDaysScraper::endTimeDefault);
		binder.forField(timePatternTextField).bind(CalendarSourceMultipleDaysScraper::timePattern, CalendarSourceMultipleDaysScraper::timePattern);
		binder.forField(dateTimeLocaleTextField).bind(CalendarSourceMultipleDaysScraper::dateTimeLocale, CalendarSourceMultipleDaysScraper::dateTimeLocale);
	}

	private void testRegex() {
		CalendarSourceMultipleDaysScraper calendarSource = new CalendarSourceMultipleDaysScraper();
		new CalendarLocation().enabled(true).addCalendarSource(calendarSource);
		generateAndShowTrace(calendarSource);
	}

	@Override
	public CalendarSourceMultipleDaysScraperForm populateWith(CalendarSource calendarSource) {
		super.populateWith(calendarSource);
		if (calendarSource instanceof CalendarSourceMultipleDaysScraper calendarSourceMultipleDaysScraper) {
			binder.readBean(calendarSourceMultipleDaysScraper);
		}
		return this;
	}

	@Override
	public CalendarSourceMultipleDaysScraperForm writeTo(CalendarSource calendarSource) throws ValidationException {
		super.writeTo(calendarSource);
		if (calendarSource instanceof CalendarSourceMultipleDaysScraper calendarSourceMultipleDaysScraper) {
			binder.writeBean(calendarSourceMultipleDaysScraper);
		}
		return this;
	}
}