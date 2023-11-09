package nl.softworks.calendarAggregator.boundary.vdn;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;
import jakarta.annotation.security.PermitAll;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Route("/")
@StyleSheet("context://../vaadin.css")
//@RolesAllowed("ROLE_PLANNER")
@PermitAll
public class MainView extends CalendarAggregatorAppLayout
implements AfterNavigationObserver
{
	private static final Logger LOG = LoggerFactory.getLogger(MainView.class);
	private static final DateTimeFormatter YYYYMMDDHHMM = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final TreeGrid<TreeNode> calendarSourceAndEventTreeGrid = new TreeGrid<>();
	private final CalendarSourceForm calendarSourceForm = new CalendarSourceForm();
	private final CalendarEventForm calendarEventForm = new CalendarEventForm();
	private List<TreeNode> treeNodes = null;

	public MainView() {
		super("Overview");
		tabs.setSelectedTab(overviewTab);

		// calendarSourceAndEventTreeGrid
		calendarSourceAndEventTreeGrid.addHierarchyColumn(TreeNode::getText).setHeader("Name");
		calendarSourceAndEventTreeGrid.addColumn(TreeNode::getStartDate).setHeader("Start");
		calendarSourceAndEventTreeGrid.addColumn(TreeNode::getEndDate).setHeader("End");

		// buttonbar
		CrudButtonbar crudButtonbar = new CrudButtonbar()
				.onInsert(this::insert)
				.onEdit(this::edit)
				.onDelete(this::delete);

		// content
		setContent(new VerticalLayout(crudButtonbar, calendarSourceAndEventTreeGrid));
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		List<CalendarSource> calendarSources = R.calendarSource().findAll();
		treeNodes = treeNodes(calendarSources, TreeNodeCalendarSource::new);
		refreshTreeGrid();
	}

	private void insert() {
	}

	private void edit() {
		// Get the selected treenode
		Set<TreeNode> selectedItems = calendarSourceAndEventTreeGrid.getSelectedItems();
		if (selectedItems.isEmpty() || selectedItems.size() > 1) {
			return;
		}
		TreeNode treeNode = selectedItems.iterator().next();

		// Create a dialog
		Dialog dialog = new Dialog();
		Button closeButton = new Button(LumoIcon.CROSS.create(), e -> dialog.close());
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		dialog.getHeader().add(closeButton);
		Button saveButton = new Button("Save", VaadinIcon.SAFE.create());
		dialog.getFooter().add(saveButton);

		// Populate with source
		if (treeNode instanceof TreeNodeCalendarSource treeNodeCalendarSource) {
			CalendarSource calendarSource = treeNodeCalendarSource.calendarSource();
			calendarSourceForm.populateWith(calendarSource);
			dialog.setHeaderTitle("Source");
			dialog.add(calendarSourceForm);
			saveButton.addClickListener(e -> {
				calendarSourceForm.writeTo(calendarSource);
				R.calendarSource().save(calendarSource);
				dialog.close();
				refreshTreeGrid();
			});
		}
		// Populate with event
		if (treeNode instanceof TreeNodeCalendarEvent treeNodeCalendarEvent) {
			CalendarEvent calendarEvent = treeNodeCalendarEvent.calendarEvent();
			calendarEventForm.populateWith(calendarEvent);
			dialog.setHeaderTitle("Event");
			dialog.add(calendarEventForm);
			saveButton.addClickListener(e -> {
				calendarEventForm.writeTo(calendarEvent);
				R.calendarEvent().save(calendarEvent);
				dialog.close();
				refreshTreeGrid();
			});
		}
		dialog.open();
	}

	private void delete() {
	}

	private void refreshTreeGrid() {
		Set<TreeNode> selectedItems = calendarSourceAndEventTreeGrid.getSelectedItems();
		calendarSourceAndEventTreeGrid.setItems(treeNodes, this::getTreeNodeChildren);
		if (selectedItems.isEmpty() || selectedItems.size() > 1) {
			return;
		}
		TreeNode treeNode = selectedItems.iterator().next();
        if (treeNode instanceof TreeNodeCalendarEvent treeNodeCalendarEvent) {
            calendarSourceAndEventTreeGrid.expand(treeNodeCalendarEvent.treeNodeCalendarSource());
        }
		calendarSourceAndEventTreeGrid.select(treeNode);
	}


	sealed interface TreeNode permits TreeNodeCalendarSource, TreeNodeCalendarEvent {
		String getText();
		String getStartDate();
		String getEndDate();
	}

	record TreeNodeCalendarSource(CalendarSource calendarSource) implements TreeNode {
		@Override
		public String getText() {
			return calendarSource().name();
		}

		@Override
		public String getStartDate() {
			return null;
		}

		@Override
		public String getEndDate() {
			return null;
		}
	}
	record TreeNodeCalendarEvent (TreeNodeCalendarSource treeNodeCalendarSource, CalendarEvent calendarEvent) implements TreeNode {
		@Override
		public String getText() {
			return calendarEvent.subject();
		}

		@Override
		public String getStartDate() {
			return calendarEvent.startDateTime().format(YYYYMMDDHHMM);
		}

		@Override
		public String getEndDate() {
			return calendarEvent.endDateTime().format(YYYYMMDDHHMM);
		}
	}

	public List<TreeNode> getTreeNodeChildren(TreeNode treeNode) {
		if (treeNode instanceof TreeNodeCalendarSource treeNodeCalendarSource) {
			return treeNodes(treeNodeCalendarSource.calendarSource().getCalendarEvents(), ce -> new TreeNodeCalendarEvent(treeNodeCalendarSource, ce));
		}
		return List.of();
	}

	private <T> List<TreeNode> treeNodes(Collection<T> businessObjects, Function<T, TreeNode> converter) {
		List<TreeNode> treeNodes = new ArrayList<>();
		businessObjects.stream().map(bo -> converter.apply(bo)).forEach(tn -> treeNodes.add(tn));
		return treeNodes;
	}
}