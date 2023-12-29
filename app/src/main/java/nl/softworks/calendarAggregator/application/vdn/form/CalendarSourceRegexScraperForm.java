package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.application.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceRegexScraper;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceScraperBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarSourceRegexScraperForm extends CalendarSourceScraperBaseForm {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceRegexScraperForm.class);

	private final Binder<CalendarSourceRegexScraper> binder = new Binder<>();

	private final TextField regexTextField = new TextField("Regex");
	private final IntegerField subjectGroupIdxIntegerField = new IntegerField("Subject group index");
	private final IntegerField startDateGroupIdxIntegerField = new IntegerField("Start date group index");
	private final IntegerField endDateGroupIdxIntegerField = new IntegerField("End date group index");
	private final TextField datePatternTextField = new TextField("Date pattern");
	private final TextField shortMonthNotationTextField = new TextField("Short month notation (" + CalendarSourceScraperBase.SHORT_MONTH_NOTATION_PATTERN + ")");
	private final Checkbox nearestYearCheckbox = new Checkbox("Use Nearest year");
	private final IntegerField startTimeGroupIdxIntegerField = new IntegerField("Start time group index");
	private final IntegerField endTimeGroupIdxIntegerField = new IntegerField("End time group index");
	private final TextField startTimeDefaultTextField = new TextField("Start time default");
	private final TextField endTimeDefaultTextField = new TextField("End time default");
	private final TextField timePatternTextField = new TextField("Time pattern");
	private final TextField dateTimeLocaleTextField = new TextField("Date Time Locale");


	public CalendarSourceRegexScraperForm() {
		setColspan(regexTextField, 2);

		Button testButton = new Button("Test", evt -> test());
		setColspan(testButton, 2);
		add(regexTextField, testButton,subjectGroupIdxIntegerField, datePatternTextField, startDateGroupIdxIntegerField, endDateGroupIdxIntegerField, shortMonthNotationTextField, nearestYearCheckbox, startTimeGroupIdxIntegerField, startTimeDefaultTextField, endTimeGroupIdxIntegerField, endTimeDefaultTextField, timePatternTextField, dateTimeLocaleTextField);

		binder.forField(regexTextField).bind(CalendarSourceRegexScraper::regex, CalendarSourceRegexScraper::regex);
		binder.forField(subjectGroupIdxIntegerField).bind(CalendarSourceRegexScraper::subjectGroupIdx, CalendarSourceRegexScraper::subjectGroupIdx);
		binder.forField(startDateGroupIdxIntegerField).bind(CalendarSourceRegexScraper::startDateGroupIdx, CalendarSourceRegexScraper::startDateGroupIdx);
		binder.forField(endDateGroupIdxIntegerField).bind(CalendarSourceRegexScraper::endDateGroupIdx, CalendarSourceRegexScraper::endDateGroupIdx);
		binder.forField(datePatternTextField).bind(CalendarSourceRegexScraper::datePattern, CalendarSourceRegexScraper::datePattern);
		binder.forField(shortMonthNotationTextField).bind(CalendarSourceRegexScraper::shortMonthNotation, CalendarSourceRegexScraper::shortMonthNotation);
		binder.forField(nearestYearCheckbox).bind(CalendarSourceRegexScraper::nearestYear, CalendarSourceRegexScraper::nearestYear);
		binder.forField(startTimeGroupIdxIntegerField).bind(CalendarSourceRegexScraper::startTimeGroupIdx, CalendarSourceRegexScraper::startTimeGroupIdx);
		binder.forField(startTimeDefaultTextField).bind(CalendarSourceRegexScraper::startTimeDefault, CalendarSourceRegexScraper::startTimeDefault);
		binder.forField(endTimeGroupIdxIntegerField).bind(CalendarSourceRegexScraper::endTimeGroupIdx, CalendarSourceRegexScraper::endTimeGroupIdx);
		binder.forField(endTimeDefaultTextField).bind(CalendarSourceRegexScraper::endTimeDefault, CalendarSourceRegexScraper::endTimeDefault);
		binder.forField(timePatternTextField).bind(CalendarSourceRegexScraper::timePattern, CalendarSourceRegexScraper::timePattern);
		binder.forField(dateTimeLocaleTextField).bind(CalendarSourceRegexScraper::dateTimeLocale, CalendarSourceRegexScraper::dateTimeLocale);
	}

	private void test() {
		generateAndShowTrace(new CalendarSourceRegexScraper());
	}

	@Override
	public CalendarSourceRegexScraperForm populateWith(CalendarSource calendarSource) {
		super.populateWith(calendarSource);
		binder.readBean((CalendarSourceRegexScraper)calendarSource);
		return this;
	}

	@Override
	public CalendarSourceRegexScraperForm writeTo(CalendarSource calendarSource) throws ValidationException {
		super.writeTo(calendarSource);
		binder.writeBean((CalendarSourceRegexScraper)calendarSource);
		return this;
	}

	public static void showInsertDialog(CalendarSource selectedCalendarSource, Runnable onInsert) {
		CalendarSourceRegexScraper calendarSource = new CalendarSourceRegexScraper();
		CalendarSourceRegexScraperForm calendarSourceForm = new CalendarSourceRegexScraperForm().populateWith(selectedCalendarSource != null ? selectedCalendarSource : calendarSource);
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