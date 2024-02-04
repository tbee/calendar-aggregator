package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.application.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceScraperBase;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceXmlScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarSourceXmlScraperForm extends CalendarSourceScraperBaseForm {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceXmlScraperForm.class);

	private final Binder<CalendarSourceXmlScraper> binder = new Binder<>();

	private final Checkbox jsonToXmlCheckbox = new Checkbox("JSON");
	private final TextField xpathTextField = new TextField("Base XPath");
	private final TextField subjectXpatjField = new TextField("Subject XPath");
	private final TextField startDateXpathField = new TextField("Start date XPath");
	private final TextField endDateXpathField = new TextField("End date XPath");
	private final TextField datePatternTextField = new TextField("Date pattern");
	private final TextField shortMonthNotationTextField = new TextField("Short month notation (" + CalendarSourceScraperBase.SHORT_MONTH_NOTATION_PATTERN + ")");
	private final Checkbox nearestYearCheckbox = new Checkbox("Use Nearest year");
	private final TextField startTimeXpathField = new TextField("Start time XPath");
	private final TextField endTimeXpathField = new TextField("End time XPath");
	private final TextField startTimeDefaultTextField = new TextField("Start time default");
	private final TextField endTimeDefaultTextField = new TextField("End time default");
	private final TextField timePatternTextField = new TextField("Time pattern");
	private final TextField dateTimeLocaleTextField = new TextField("Date Time Locale");


	public CalendarSourceXmlScraperForm() {
		setColspan(xpathTextField, 2);

		Button testButton = new Button("Test", evt -> test());
		setColspan(testButton, 2);
		add(xpathTextField, testButton, subjectXpatjField, datePatternTextField, startDateXpathField, endDateXpathField, shortMonthNotationTextField, nearestYearCheckbox, startTimeXpathField, startTimeDefaultTextField, endTimeXpathField, endTimeDefaultTextField, timePatternTextField, dateTimeLocaleTextField, jsonToXmlCheckbox);

		binder.forField(xpathTextField).bind(CalendarSourceXmlScraper::xpath, CalendarSourceXmlScraper::xpath);
		binder.forField(subjectXpatjField).bind(CalendarSourceXmlScraper::subjectXpath, CalendarSourceXmlScraper::subjectXpath);
		binder.forField(startDateXpathField).bind(CalendarSourceXmlScraper::startdateXpath, CalendarSourceXmlScraper::startdateXpath);
		binder.forField(endDateXpathField).bind(CalendarSourceXmlScraper::enddateXpath, CalendarSourceXmlScraper::enddateXpath);
		binder.forField(datePatternTextField).bind(CalendarSourceXmlScraper::datePattern, CalendarSourceXmlScraper::datePattern);
		binder.forField(shortMonthNotationTextField).bind(CalendarSourceXmlScraper::shortMonthNotation, CalendarSourceXmlScraper::shortMonthNotation);
		binder.forField(nearestYearCheckbox).bind(CalendarSourceXmlScraper::nearestYear, CalendarSourceXmlScraper::nearestYear);
		binder.forField(startTimeXpathField).bind(CalendarSourceXmlScraper::starttimeXpath, CalendarSourceXmlScraper::starttimeXpath);
		binder.forField(startTimeDefaultTextField).bind(CalendarSourceXmlScraper::startTimeDefault, CalendarSourceXmlScraper::startTimeDefault);
		binder.forField(endTimeXpathField).bind(CalendarSourceXmlScraper::endtimeXpath, CalendarSourceXmlScraper::endtimeXpath);
		binder.forField(endTimeDefaultTextField).bind(CalendarSourceXmlScraper::endTimeDefault, CalendarSourceXmlScraper::endTimeDefault);
		binder.forField(timePatternTextField).bind(CalendarSourceXmlScraper::timePattern, CalendarSourceXmlScraper::timePattern);
		binder.forField(dateTimeLocaleTextField).bind(CalendarSourceXmlScraper::dateTimeLocale, CalendarSourceXmlScraper::dateTimeLocale);
		binder.forField(jsonToXmlCheckbox).bind(CalendarSourceXmlScraper::jsonToXml, CalendarSourceXmlScraper::jsonToXml);
	}

	private void test() {
		generateAndShowTrace(new CalendarSourceXmlScraper());
	}

	@Override
	public CalendarSourceXmlScraperForm populateWith(CalendarSource calendarSource) {
		super.populateWith(calendarSource);
		binder.readBean((CalendarSourceXmlScraper)calendarSource);
		return this;
	}

	@Override
	public CalendarSourceXmlScraperForm writeTo(CalendarSource calendarSource) throws ValidationException {
		super.writeTo(calendarSource);
		binder.writeBean((CalendarSourceXmlScraper)calendarSource);
		return this;
	}

	public static void showInsertDialog(CalendarSource selectedCalendarSource, Runnable onInsert) {
		CalendarSourceXmlScraper calendarSource = new CalendarSourceXmlScraper();
		CalendarSourceXmlScraperForm calendarSourceForm = new CalendarSourceXmlScraperForm().populateWith(selectedCalendarSource != null ? selectedCalendarSource : calendarSource);
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