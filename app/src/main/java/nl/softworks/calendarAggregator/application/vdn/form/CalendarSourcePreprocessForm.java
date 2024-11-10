package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourcePreprocess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarSourcePreprocessForm extends AbstractCrudForm<CalendarSourcePreprocess> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarSourcePreprocessForm.class);

	private final Binder<CalendarSourcePreprocess> binder = new Binder<>();

	private final TextField oldValueTextField = new TextField("Regexp");
	private final TextField newValueTextField = new TextField("Replacement");

	public CalendarSourcePreprocessForm() {
		add(oldValueTextField, newValueTextField);

		binder.forField(oldValueTextField).bind(CalendarSourcePreprocess::oldValue, CalendarSourcePreprocess::oldValue);
		binder.forField(newValueTextField).bind(CalendarSourcePreprocess::newValue, CalendarSourcePreprocess::newValue);
	}

	@Override
	public CalendarSourcePreprocessForm populateWith(CalendarSourcePreprocess calendarSourcePreprocess) {
		binder.readBean(calendarSourcePreprocess);
		return this;
	}

	@Override
	public CalendarSourcePreprocessForm writeTo(CalendarSourcePreprocess calendarSourcePreprocess) throws ValidationException {
		binder.writeBean(calendarSourcePreprocess);
		return this;
	}
}