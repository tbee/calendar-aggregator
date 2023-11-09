package nl.softworks.calendarAggregator.boundary.vdn;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;
import jakarta.annotation.security.PermitAll;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.Timezone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Route("/timezone")
@StyleSheet("context://../vaadin.css")
//@RolesAllowed("ROLE_PLANNER")
@PermitAll
public class TimezoneView extends CalendarAggregatorAppLayout
implements AfterNavigationObserver {
	private static final Logger LOG = LoggerFactory.getLogger(TimezoneView.class);

	private final Grid<Timezone> timezoneTreeGrid = new Grid<>();

	public TimezoneView() {
		super("Timezones");
		tabs.setSelectedTab(timezoneTab);

		// timezoneTreeGrid
		timezoneTreeGrid.addColumn(Timezone::name).setHeader("Name");

		// crudButtonbar
		CrudButtonbar crudButtonbar = new CrudButtonbar()
				.onInsert(this::insert)
				.onEdit(this::edit)
				.onDelete(this::delete);

		// content
		setContent(new VerticalLayout(crudButtonbar, timezoneTreeGrid));
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		loadGrid();
	}

	private void insert() {

	}

	private void edit() {
		// Get the selected treenode
		Timezone timezone = getSelectedTimezone();
		if (timezone == null) {
			return;
		}

		// Create a dialog
		Dialog dialog = new Dialog();
		Button closeButton = new Button(LumoIcon.CROSS.create(), e -> dialog.close());
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		dialog.getHeader().add(closeButton);
		Button saveButton = new Button("Save", VaadinIcon.SAFE.create());
		dialog.getFooter().add(saveButton);

		// Populate with source
		TimezoneForm timezoneForm = new TimezoneForm();
		timezoneForm.populateWith(timezone);
		dialog.setHeaderTitle("Timezone");
		dialog.add(timezoneForm);
		saveButton.addClickListener(e -> {
			timezoneForm.writeTo(timezone);
			R.timezoneRepo().save(timezone);
			dialog.close();
			loadGrid();
		});

		dialog.open();
	}

	private void loadGrid() {
		Timezone selectedTimezone = getSelectedTimezone();

		List<Timezone> timezones = R.timezoneRepo().findAll();
		timezones.sort((timezone1, timezone2) -> timezone1.name().compareTo(timezone2.name()));
		timezoneTreeGrid.setItems(timezones);

		if (selectedTimezone != null) {
			Timezone timezone = timezones.stream().filter(tz -> tz.id() == selectedTimezone.id()).findFirst().orElse(null);
			if (timezone != null) {
				timezoneTreeGrid.select(timezone);
			}
		}
	}

	private void delete() {

	}

	private Timezone getSelectedTimezone() {
		// Get the selected treenode
		Set<Timezone> selectedItems = timezoneTreeGrid.getSelectedItems();
		if (selectedItems.isEmpty() || selectedItems.size() > 1) {
			return null;
		}
		Timezone timezone = selectedItems.iterator().next();
		return timezone;
	}
}