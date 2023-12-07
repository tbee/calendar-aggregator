package nl.softworks.calendarAggregator.boundary.vdn.form;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.domain.entity.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.jakarta.validator.UrlValidatorImpl;

public class SettingsForm extends FormLayout {
	private static final Logger LOG = LoggerFactory.getLogger(SettingsForm.class);

	private final Binder<Settings> binder = new Binder<>();

	private final TextField websiteBaseurlTextField = new TextField("Website baseurl");
	private final TextField titleTextField = new TextField("Title");
	private final TextField subtitleTextField = new TextField("Subtitle");
	private final TextArea disclaimerTextField = new TextArea("Disclaimer");

	public SettingsForm() {
		add(websiteBaseurlTextField, titleTextField, subtitleTextField, disclaimerTextField);
		setColspan(websiteBaseurlTextField, 2);
		setColspan(disclaimerTextField, 2);

		binder.forField(websiteBaseurlTextField).withValidator(s -> UrlValidatorImpl.isValid(s), "Illegal URL").bind(Settings::websiteBaseurl, Settings::websiteBaseurl);
		binder.forField(titleTextField).bind(Settings::title, Settings::title);
		binder.forField(subtitleTextField).bind(Settings::subtitle, Settings::subtitle);
		binder.forField(disclaimerTextField).bind(Settings::disclaimer, Settings::disclaimer);
	}

	public SettingsForm populateWith(Settings Settings) {
		binder.readBean(Settings);
		return this;
	}

	public SettingsForm writeTo(Settings Settings) throws ValidationException {
		binder.writeBean(Settings);
		return this;
	}
}