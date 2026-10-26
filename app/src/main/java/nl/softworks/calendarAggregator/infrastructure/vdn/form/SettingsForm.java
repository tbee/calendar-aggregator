package nl.softworks.calendarAggregator.infrastructure.vdn.form;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.domain.entity.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.webstack.vdn.component.ImageUpload;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class SettingsForm extends FormLayout {
	private static final Logger LOGGER = LoggerFactory.getLogger(SettingsForm.class);

	private final Binder<Settings> binder = new Binder<>();

	private final TextField titleTextField = new TextField("Title");
	private final TextField subtitleTextField = new TextField("Subtitle");
	private final TextArea disclaimerTextField = new TextArea("Disclaimer");
	private final ImageUpload logoImageUpload = new ImageUpload()
			.filetypes(new String[]{"image/jpeg", "image/png", "image/webp", "image/svg+xml"})
			.maxFileSize(2 * 1024 * 1024)
			.imageWidth("300px")
			.imageHeight("300px")
			.id("logoImageUpload");

	private byte[] logoBytes;
	private String logoMimetype;

	public SettingsForm() {
		add(titleTextField, subtitleTextField, disclaimerTextField, logoImageUpload);
		setColspan(disclaimerTextField, 2);
		setColspan(logoImageUpload, 2);

		binder.forField(titleTextField).bind(Settings::title, Settings::title);
		binder.forField(subtitleTextField).bind(Settings::subtitle, Settings::subtitle);
		binder.forField(disclaimerTextField).bind(Settings::disclaimer, Settings::disclaimer);
	}

	public SettingsForm populateWith(Settings Settings) {
		binder.readBean(Settings);
		logoBytes = Settings.logo();
		logoMimetype = Settings.logoMimetype();
		if (logoBytes != null && logoBytes.length > 0) {
			// browsers sniff the actual image type, so a generic mime is fine
			logoImageUpload.src("data:image/*;base64," + Base64.getEncoder().encodeToString(logoBytes));
		}
		return this;
	}

	public SettingsForm writeTo(Settings Settings) throws ValidationException {
		binder.writeBean(Settings);
		if (logoImageUpload.hasUpload()) {
			try (InputStream inputStream = logoImageUpload.inputStream()) {
				logoBytes = inputStream.readAllBytes();
				logoMimetype = logoImageUpload.mimeType();
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		else if (!logoImageUpload.hasImage()) {
			logoBytes = null;
			logoMimetype = null;
		}
		Settings.logo(logoBytes);
		Settings.logoMimetype(logoMimetype);
		return this;
	}
}
