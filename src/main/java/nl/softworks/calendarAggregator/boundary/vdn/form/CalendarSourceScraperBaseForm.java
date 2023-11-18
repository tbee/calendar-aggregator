package nl.softworks.calendarAggregator.boundary.vdn.form;

import com.vaadin.flow.component.textfield.TextField;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceScraperBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarSourceScraperBaseForm extends CalendarSourceForm {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceScraperBaseForm.class);

	private final TextField scrapeURLTextField = new TextField("Scrape URL");

	public CalendarSourceScraperBaseForm() {
		setColspan(scrapeURLTextField, 2);
		add(scrapeURLTextField);
	}

	@Override
	public CalendarSourceScraperBaseForm populateWith(CalendarSource calendarSource) {
		CalendarSourceScraperBase calendarSourceScraperBase = (CalendarSourceScraperBase)calendarSource;
		super.populateWith(calendarSourceScraperBase);
		scrapeURLTextField.setValue(calendarSourceScraperBase.scrapeUrl() == null ? "" : calendarSourceScraperBase.scrapeUrl());
		return this;
	}

	@Override
	public CalendarSourceScraperBaseForm writeTo(CalendarSource calendarSource) {
		CalendarSourceScraperBase calendarSourceScraperBase = (CalendarSourceScraperBase)calendarSource;
		super.writeTo(calendarSourceScraperBase);
		calendarSourceScraperBase.scrapeUrl(scrapeURLTextField.getValue());
		return this;
	}
}