package nl.softworks.calendarAggregator.boundary.vdn;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.renderer.BasicRenderer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.Timezone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CalendarSourceForm extends FormLayout {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceForm.class);

	private final TextField nameTextField = new TextField("Name");
	private final TextField urlTextField = new TextField("Url");
	private final NumberField latNumberField = new NumberField("LAT");
	private final NumberField lonNumberField = new NumberField("LON");
	private final ComboBox<Timezone> timezoneComboBox = new ComboBox<>("Timezone");

	public CalendarSourceForm() {
		timezoneComboBox.setItemLabelGenerator(timezone -> timezone.name());
		timezoneComboBox.setRenderer(new ComponentRenderer<>(timezone -> {
			Span nameSpan = new Span(timezone.name());
			return nameSpan;
		}));
		add(nameTextField, urlTextField, latNumberField, lonNumberField, timezoneComboBox);
	}

	public void populateWith(CalendarSource calendarSource) {
		timezoneComboBox.setItems(R.timezoneRepo().findAll());

		nameTextField.setValue(calendarSource == null ? "" : calendarSource.name());
		urlTextField.setValue(calendarSource == null ? "" : calendarSource.url());
		latNumberField.setValue(calendarSource == null ? 0 : calendarSource.lat());
		lonNumberField.setValue(calendarSource == null ? 0 : calendarSource.lon());
		timezoneComboBox.setValue(calendarSource == null ? null : calendarSource.timezone());
	}

	public void writeTo(CalendarSource calendarSource) {
		calendarSource.name(nameTextField.getValue());
		calendarSource.url(urlTextField.getValue());
		calendarSource.lat(latNumberField.getValue());
		calendarSource.lon(lonNumberField.getValue());
		calendarSource.timezone(timezoneComboBox.getValue());
	}
}