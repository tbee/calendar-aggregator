package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceScraperBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarSourceScraperBaseForm extends CalendarSourceForm {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarSourceScraperBaseForm.class);

	private final Binder<CalendarSourceScraperBase> binder = new Binder<>();

	private final TextField scrapeURLTextField = new TextField("Scrape URL");

	protected final FormLayout scrapeFormLayout;

	public CalendarSourceScraperBaseForm() {
		scrapeURLTextField.setTooltipText("Use `=@{now.plusYears(1).format(yyyy_MM_dd)}` to insert code.");

		scrapeFormLayout = addAsFormlayoutInAccordion("Scrape", scrapeURLTextField);
		scrapeFormLayout.setColspan(scrapeURLTextField, 2);

		binder.forField(scrapeURLTextField)
				//.withValidator(s -> UrlValidatorImpl.isValid(s), "Illegal URL") // conflicts with MVEL expressions, moved to the entity
				.bind(CalendarSourceScraperBase::scrapeUrl, CalendarSourceScraperBase::scrapeUrl);
	}

	@Override
	public CalendarSourceScraperBaseForm populateWith(CalendarSource calendarSource) {
		super.populateWith(calendarSource);
		if (calendarSource instanceof CalendarSourceScraperBase calendarSourceScraperBase) {
			binder.readBean(calendarSourceScraperBase);
		}
		return this;
	}

	@Override
	public CalendarSourceScraperBaseForm writeTo(CalendarSource calendarSource) throws ValidationException {
		super.writeTo(calendarSource);
		if (calendarSource instanceof CalendarSourceScraperBase calendarSourceScraperBase) {
			binder.writeBean(calendarSourceScraperBase);
		}
		return this;
	}
}