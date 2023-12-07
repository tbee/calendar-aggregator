package nl.softworks.calendarAggregator.boundary.vdn.view;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import nl.softworks.calendarAggregator.boundary.vdn.CalendarAggregatorAppLayout;
import nl.softworks.calendarAggregator.boundary.vdn.component.CrudButtonbar;
import nl.softworks.calendarAggregator.boundary.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.boundary.vdn.form.SettingsForm;
import nl.softworks.calendarAggregator.boundary.vdn.form.TimezoneForm;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.Settings;
import nl.softworks.calendarAggregator.domain.entity.Timezone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

@Route("/settings")
@StyleSheet("context://../vaadin.css")
@RolesAllowed("ROLE_ADMIN")
public class SettingsView extends CalendarAggregatorAppLayout
implements AfterNavigationObserver {
	private static final Logger LOG = LoggerFactory.getLogger(SettingsView.class);

	private final SettingsForm settingsForm = new SettingsForm();
	private final Button saveButton = new Button("Save", e -> save());

	public SettingsView() {
		super("Settings");
		tabs.setSelectedTab(settingsTab);

		settingsForm.setSizeFull();
		setContent(new VerticalLayout(settingsForm, saveButton));
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		settingsForm.populateWith(Settings.get());
	}

	private void save() {
		try {
			Settings settings = Settings.get();
			settingsForm.writeTo(settings);
			R.settings().save(settings);
			showSuccessNotification("Saved");
		} catch (ValidationException e) {
			throw new RuntimeException(e);
		}
	}
}