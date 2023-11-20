package nl.softworks.calendarAggregator.boundary.vdn.form;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import nl.softworks.calendarAggregator.boundary.vdn.component.CancelDialog;
import nl.softworks.calendarAggregator.boundary.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceRegexScraper;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceScraperBase;
import nl.softworks.calendarAggregator.domain.entity.Timezone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class CalendarSourceRegexScraperForm extends CalendarSourceScraperBaseForm {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceRegexScraperForm.class);

	private final Binder<CalendarSourceRegexScraper> binder = new Binder<>();

	private final TextArea contentTextField = new TextArea("Content");
	private final TextField regexTextField = new TextField("Regex");
	private final IntegerField subjectGroupIdxIntegerField = new IntegerField("Subject group index");
	private final IntegerField startDateGroupIdxIntegerField = new IntegerField("Start date group index");
	private final IntegerField endDateGroupIdxIntegerField = new IntegerField("End date group index");
	private final TextField datePatternTextField = new TextField("Date pattern");
	private final IntegerField startTimeGroupIdxIntegerField = new IntegerField("Start time group index");
	private final IntegerField endTimeGroupIdxIntegerField = new IntegerField("End time group index");
	private final TextField startTimeDefaultTextField = new TextField("Start time default");
	private final TextField endTimeDefaultTextField = new TextField("End time default");
	private final TextField timePatternTextField = new TextField("Time pattern");
	private final TextField dateTimeLocaleTextField = new TextField("Date Time Locale");


	public CalendarSourceRegexScraperForm() {
		setColspan(contentTextField, 2);
		setColspan(regexTextField, 2);
		add(contentTextField, regexTextField, subjectGroupIdxIntegerField, startDateGroupIdxIntegerField, endDateGroupIdxIntegerField, datePatternTextField, startTimeGroupIdxIntegerField, startTimeDefaultTextField, endTimeGroupIdxIntegerField, endTimeDefaultTextField, timePatternTextField, dateTimeLocaleTextField);

		binder.forField(contentTextField).bind(CalendarSourceRegexScraper::content, CalendarSourceRegexScraper::content);
		binder.forField(regexTextField).bind(CalendarSourceRegexScraper::regex, CalendarSourceRegexScraper::regex);
		binder.forField(subjectGroupIdxIntegerField).bind(CalendarSourceRegexScraper::subjectGroupIdx, CalendarSourceRegexScraper::subjectGroupIdx);
		binder.forField(startDateGroupIdxIntegerField).bind(CalendarSourceRegexScraper::startDateGroupIdx, CalendarSourceRegexScraper::startDateGroupIdx);
		binder.forField(endDateGroupIdxIntegerField).bind(CalendarSourceRegexScraper::endDateGroupIdx, CalendarSourceRegexScraper::endDateGroupIdx);
		binder.forField(datePatternTextField).bind(CalendarSourceRegexScraper::datePattern, CalendarSourceRegexScraper::datePattern);
		binder.forField(startTimeGroupIdxIntegerField).bind(CalendarSourceRegexScraper::startTimeGroupIdx, CalendarSourceRegexScraper::startTimeGroupIdx);
		binder.forField(startTimeDefaultTextField).bind(CalendarSourceRegexScraper::startTimeDefault, CalendarSourceRegexScraper::startTimeDefault);
		binder.forField(endTimeGroupIdxIntegerField).bind(CalendarSourceRegexScraper::endTimeGroupIdx, CalendarSourceRegexScraper::endTimeGroupIdx);
		binder.forField(endTimeDefaultTextField).bind(CalendarSourceRegexScraper::endTimeDefault, CalendarSourceRegexScraper::endTimeDefault);
		binder.forField(timePatternTextField).bind(CalendarSourceRegexScraper::timePattern, CalendarSourceRegexScraper::timePattern);
		binder.forField(dateTimeLocaleTextField).bind(CalendarSourceRegexScraper::dateTimeLocale, CalendarSourceRegexScraper::dateTimeLocale);

		regexTextField.getElement().addEventListener("dblclick", e -> testRegex());
	}

	private void testRegex() {
		try {
			CalendarSourceRegexScraper calendarSourceRegexScraper = new CalendarSourceRegexScraper();
			binder.writeBean(calendarSourceRegexScraper);

			StringBuilder stringBuilder = new StringBuilder();
			List<CalendarEvent> calendarEvents = calendarSourceRegexScraper.generateEvents(stringBuilder);
			String calendarEventsString = calendarEvents.stream().map(s -> s + "\n").collect(Collectors.joining());

			TextArea textArea = new TextArea("Result", stringBuilder.toString() + "\n\n" + calendarEventsString, "");
			textArea.setSizeFull();

			CancelDialog cancelDialog = new CancelDialog("Regexp", textArea);
			cancelDialog.setSizeFull();
			cancelDialog.open();
		} catch (ValidationException e) {
			Notification.show(e.toString(), 5000, Notification.Position.BOTTOM_CENTER);
		}
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

	public static void showInsertDialog(Runnable onInsert) {
		CalendarSourceRegexScraper calendarSource = new CalendarSourceRegexScraper();
		CalendarSourceRegexScraperForm calendarSourceForm = new CalendarSourceRegexScraperForm().populateWith(calendarSource);
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