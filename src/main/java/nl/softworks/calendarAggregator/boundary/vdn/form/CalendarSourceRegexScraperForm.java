package nl.softworks.calendarAggregator.boundary.vdn.form;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import nl.softworks.calendarAggregator.boundary.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceRegexScraper;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceScraperBase;
import nl.softworks.calendarAggregator.domain.entity.Timezone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarSourceRegexScraperForm extends CalendarSourceScraperBaseForm {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceRegexScraperForm.class);

	private final TextArea contentTextField = new TextArea("Content");
	private final TextField regexTextField = new TextField("Regex");
	private final IntegerField subjectGroupIdxIntegerField = new IntegerField("Subject group index");
	private final IntegerField startDateGroupIdxIntegerField = new IntegerField("Start date group index");
	private final IntegerField endDateGroupIdxIntegerField = new IntegerField("End date group index");
	private final TextField datePatternTextField = new TextField("Date pattern");
	private final IntegerField startTimeGroupIdxIntegerField = new IntegerField("Start time group index");
	private final IntegerField endTimeGroupIdxIntegerField = new IntegerField("End time group index");
	private final TextField timePatternTextField = new TextField("Time pattern");
	private final TextField dateTimeLocaleTextField = new TextField("Date Time Locale");


	public CalendarSourceRegexScraperForm() {
		setColspan(contentTextField, 2);
		setColspan(regexTextField, 2);
		add(contentTextField, regexTextField, subjectGroupIdxIntegerField, startDateGroupIdxIntegerField, endDateGroupIdxIntegerField, datePatternTextField, startTimeGroupIdxIntegerField, endTimeGroupIdxIntegerField, timePatternTextField,dateTimeLocaleTextField);
	}

	@Override
	public CalendarSourceRegexScraperForm populateWith(CalendarSource calendarSource) {
		CalendarSourceRegexScraper calendarSourceRegexScraper = (CalendarSourceRegexScraper)calendarSource;
		super.populateWith(calendarSourceRegexScraper);
		contentTextField.setValue(calendarSourceRegexScraper.content() == null ? "" : calendarSourceRegexScraper.content());
		regexTextField.setValue(calendarSourceRegexScraper.regex() == null ? "" : calendarSourceRegexScraper.regex());
		subjectGroupIdxIntegerField.setValue(calendarSourceRegexScraper.subjectGroupIdx());
		startDateGroupIdxIntegerField.setValue(calendarSourceRegexScraper.startDateGroupIdx());
		endDateGroupIdxIntegerField.setValue(calendarSourceRegexScraper.endDateGroupIdx());
		datePatternTextField.setValue(calendarSourceRegexScraper.datePattern() == null ? "" : calendarSourceRegexScraper.datePattern());
		startTimeGroupIdxIntegerField.setValue(calendarSourceRegexScraper.startTimeGroupIdx());
		endTimeGroupIdxIntegerField.setValue(calendarSourceRegexScraper.endTimeGroupIdx());
		timePatternTextField.setValue(calendarSourceRegexScraper.timePattern() == null ? "" : calendarSourceRegexScraper.timePattern());
		dateTimeLocaleTextField.setValue(calendarSourceRegexScraper.dateTimeLocale() == null ? "" : calendarSourceRegexScraper.dateTimeLocale());
		return this;
	}

	@Override
	public CalendarSourceRegexScraperForm writeTo(CalendarSource calendarSource) {
		CalendarSourceRegexScraper calendarSourceRegexScraper = (CalendarSourceRegexScraper)calendarSource;
		super.writeTo(calendarSourceRegexScraper);
		calendarSourceRegexScraper.content(contentTextField.getValue());
		calendarSourceRegexScraper.regex(regexTextField.getValue());
		calendarSourceRegexScraper.subjectGroupIdx(subjectGroupIdxIntegerField.getValue());
		calendarSourceRegexScraper.startDateGroupIdx(startDateGroupIdxIntegerField.getValue());
		calendarSourceRegexScraper.endDateGroupIdx(endDateGroupIdxIntegerField.getValue());
		calendarSourceRegexScraper.datePattern(datePatternTextField.getValue());
		calendarSourceRegexScraper.startTimeGroupIdx(startTimeGroupIdxIntegerField.getValue());
		calendarSourceRegexScraper.endTimeGroupIdx(endTimeGroupIdxIntegerField.getValue());
		calendarSourceRegexScraper.timePattern(timePatternTextField.getValue());
		calendarSourceRegexScraper.datePattern(dateTimeLocaleTextField.getValue());
		return this;
	}

	public static void showInsertDialog(Runnable onInsert) {
		CalendarSourceRegexScraper calendarSource = new CalendarSourceRegexScraper();
		CalendarSourceRegexScraperForm calendarSourceForm = new CalendarSourceRegexScraperForm().populateWith(calendarSource);
		new OkCancelDialog("Source", calendarSourceForm)
				.okLabel("Save")
				.onOk(() -> {
					calendarSourceForm.writeTo(calendarSource);
					R.calendarSource().save(calendarSource);
					onInsert.run();
				})
				.open();
	}
}