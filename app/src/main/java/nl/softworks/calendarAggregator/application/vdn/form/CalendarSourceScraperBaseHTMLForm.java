package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceScraperBase;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceScraperBaseHTML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarSourceScraperBaseHTMLForm extends CalendarSourceScraperBaseForm {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarSourceScraperBaseHTMLForm.class);

	private final Binder<CalendarSourceScraperBaseHTML> binder = new Binder<>();

	private final TextField scrapeBlockStartTextField = new TextField("Scrape block start");
	private final TextField scrapeBlockEndTextField = new TextField("Scrape block end");
	private final TextField removeCharsTextField = new TextField("Remove chars");

	public CalendarSourceScraperBaseHTMLForm() {
		scrapeFormLayout.add(scrapeBlockStartTextField, scrapeBlockEndTextField, removeCharsTextField);

		binder.forField(scrapeBlockStartTextField).bind(CalendarSourceScraperBaseHTML::scrapeBlockStart, CalendarSourceScraperBaseHTML::scrapeBlockStart);
		binder.forField(scrapeBlockEndTextField).bind(CalendarSourceScraperBaseHTML::scrapeBlockEnd, CalendarSourceScraperBaseHTML::scrapeBlockEnd);
		binder.forField(removeCharsTextField).bind(CalendarSourceScraperBaseHTML::removeChars, CalendarSourceScraperBaseHTML::removeChars);
	}

	@Override
	public CalendarSourceScraperBaseHTMLForm populateWith(CalendarSource calendarSource) {
		super.populateWith(calendarSource);
		if (calendarSource instanceof CalendarSourceScraperBaseHTML calendarSourceScraperBaseHTML) {
			binder.readBean(calendarSourceScraperBaseHTML);
		}
		return this;
	}

	@Override
	public CalendarSourceScraperBaseHTMLForm writeTo(CalendarSource calendarSource) throws ValidationException {
		super.writeTo(calendarSource);
		if (calendarSource instanceof CalendarSourceScraperBaseHTML calendarSourceScraperBaseHTML) {
			binder.writeBean(calendarSourceScraperBaseHTML);
		}
		return this;
	}
}