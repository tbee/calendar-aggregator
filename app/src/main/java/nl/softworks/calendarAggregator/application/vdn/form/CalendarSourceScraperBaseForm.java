package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceScraperBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.jakarta.validator.UrlValidatorImpl;

public class CalendarSourceScraperBaseForm extends CalendarSourceForm {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceScraperBaseForm.class);

	private final Binder<CalendarSourceScraperBase> binder = new Binder<>();

	private final TextField scrapeURLTextField = new TextField("Scrape URL");
	private final TextField scrapeBlockStartTextField = new TextField("Scrape block start");
	private final TextField scrapeBlockEndTextField = new TextField("Scrape block end");
	private final TextField removeCharsTextField = new TextField("Remove chars");

	public CalendarSourceScraperBaseForm() {
		setColspan(scrapeURLTextField, 2);
		add(scrapeURLTextField, scrapeBlockStartTextField, scrapeBlockEndTextField, removeCharsTextField);

		binder.forField(scrapeURLTextField).withValidator(s -> UrlValidatorImpl.isValid(s), "Illegal URL").bind(CalendarSourceScraperBase::scrapeUrl, CalendarSourceScraperBase::scrapeUrl);
		binder.forField(scrapeBlockStartTextField).bind(CalendarSourceScraperBase::scrapeBlockStart, CalendarSourceScraperBase::scrapeBlockStart);
		binder.forField(scrapeBlockEndTextField).bind(CalendarSourceScraperBase::scrapeBlockEnd, CalendarSourceScraperBase::scrapeBlockEnd);
		binder.forField(removeCharsTextField).bind(CalendarSourceScraperBase::removeChars, CalendarSourceScraperBase::removeChars);
	}

	@Override
	public CalendarSourceScraperBaseForm populateWith(CalendarSource calendarSource) {
		super.populateWith(calendarSource);
		binder.readBean((CalendarSourceScraperBase)calendarSource);
		return this;
	}

	@Override
	public CalendarSourceScraperBaseForm writeTo(CalendarSource calendarSource) throws ValidationException {
		super.writeTo(calendarSource);
		binder.writeBean((CalendarSourceScraperBase)calendarSource);
		return this;
	}
}