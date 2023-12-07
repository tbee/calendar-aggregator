package nl.softworks.calendarAggregator.boundary.vdn.view;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import nl.softworks.calendarAggregator.boundary.vdn.CalendarAggregatorAppLayout;
import nl.softworks.calendarAggregator.boundary.vdn.component.CrudButtonbar;
import nl.softworks.calendarAggregator.boundary.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.boundary.vdn.form.TimezoneForm;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.Timezone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

@Route("/timezone")
@StyleSheet("context://../vaadin.css")
@RolesAllowed("ROLE_ADMIN")
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
				.onReload(this::reloadGrid)
				.onInsert(() -> TimezoneForm.showInsertDialog(this::reloadGrid))
				.onEdit(this::edit)
				.onDelete(this::delete);

		// content
		setContent(new VerticalLayout(crudButtonbar, timezoneTreeGrid));
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		reloadGrid();
	}

	private void edit() {

		// Get the selected treenode
		Timezone timezone = getSelectedTimezone();
		if (timezone == null) {
			return;
		}

		// Dialog
		TimezoneForm timezoneForm = new TimezoneForm().populateWith(timezone);
		new OkCancelDialog("Timezone", timezoneForm)
				.okLabel("Save")
				.onOk(() -> {
					timezoneForm.writeTo(timezone);
					R.timezone().save(timezone);
					reloadGrid();
				})
				.open();
	}

	private void delete() {
		Timezone timezone = getSelectedTimezone();
		if (timezone == null) {
			return;
		}

		new OkCancelDialog("Remove " + timezone.name(), new NativeLabel("Are you sure?"))
				.okLabel("Yes")
				.onOk(() -> {
					R.timezone().delete(timezone);
					reloadGrid();
				})
				.open();

	}

	private void reloadGrid() {
		// Remember selection
		Timezone selectedTimezone = getSelectedTimezone();

		// Reload timezones
		List<Timezone> timezones = R.timezone().findAll();
		timezones.sort((timezone1, timezone2) -> timezone1.name().compareTo(timezone2.name()));
		timezoneTreeGrid.setItems(timezones);

		// Reselect
		// TODO: make sure the select node is one from the treeNodes collection, not the old node, otherwise lazy lock goes wrong
//		if (selectedTimezone != null) {
//			timezoneTreeGrid.select(selectedTimezone);
//		}
	}

	private Timezone getSelectedTimezone() {
		Set<Timezone> selectedItems = timezoneTreeGrid.getSelectedItems();
		if (selectedItems.isEmpty() || selectedItems.size() > 1) {
			return null;
		}
		Timezone timezone = selectedItems.iterator().next();
		return timezone;
	}
}