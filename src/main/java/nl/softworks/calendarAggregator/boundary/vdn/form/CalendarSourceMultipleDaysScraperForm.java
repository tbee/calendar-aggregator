package nl.softworks.calendarAggregator.boundary.vdn.form;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.boundary.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceMultipleDaysScraper;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceMultipleDaysScraper;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceRegexScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarSourceMultipleDaysScraperForm extends CalendarSourceScraperBaseForm {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceMultipleDaysScraperForm.class);

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
		setColspan(regexTextField, 2);

		Button testButton = new Button("Test Regex", evt -> testRegex());
		setColspan(testButton, 2);
		add(regexTextField, testButton, datePatternTextField, nearestYearCheckbox, startTimeDefaultTextField, endTimeDefaultTextField, timePatternTextField, dateTimeLocaleTextField);

		binder.forField(regexTextField).bind(CalendarSourceMultipleDaysScraper::regex, CalendarSourceMultipleDaysScraper::regex);
		binder.forField(datePatternTextField).bind(CalendarSourceMultipleDaysScraper::datePattern, CalendarSourceMultipleDaysScraper::datePattern);
		binder.forField(nearestYearCheckbox).bind(CalendarSourceMultipleDaysScraper::nearestYear, CalendarSourceMultipleDaysScraper::nearestYear);
		binder.forField(startTimeDefaultTextField).bind(CalendarSourceMultipleDaysScraper::startTimeDefault, CalendarSourceMultipleDaysScraper::startTimeDefault);
		binder.forField(endTimeDefaultTextField).bind(CalendarSourceMultipleDaysScraper::endTimeDefault, CalendarSourceMultipleDaysScraper::endTimeDefault);
		binder.forField(timePatternTextField).bind(CalendarSourceMultipleDaysScraper::timePattern, CalendarSourceMultipleDaysScraper::timePattern);
		binder.forField(dateTimeLocaleTextField).bind(CalendarSourceMultipleDaysScraper::dateTimeLocale, CalendarSourceMultipleDaysScraper::dateTimeLocale);
	}

	private void testRegex() {
		generateAndShowTrace(new CalendarSourceMultipleDaysScraper());
	}

	@Override
	public CalendarSourceMultipleDaysScraperForm populateWith(CalendarSource calendarSource) {
		super.populateWith(calendarSource);
		binder.readBean((CalendarSourceMultipleDaysScraper)calendarSource);
		return this;
	}

	@Override
	public CalendarSourceMultipleDaysScraperForm writeTo(CalendarSource calendarSource) throws ValidationException {
		super.writeTo(calendarSource);
		binder.writeBean((CalendarSourceMultipleDaysScraper)calendarSource);
		return this;
	}

	public static void showInsertDialog(CalendarSource selectedCalendarSource, Runnable onInsert) {
		CalendarSourceMultipleDaysScraper calendarSource = new CalendarSourceMultipleDaysScraper();
		CalendarSourceMultipleDaysScraperForm calendarSourceForm = new CalendarSourceMultipleDaysScraperForm().populateWith(selectedCalendarSource != null ? selectedCalendarSource : calendarSource);
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