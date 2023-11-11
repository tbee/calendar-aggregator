package nl.softworks.calendarAggregator.boundary.vdn;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import nl.softworks.calendarAggregator.boundary.vdn.component.CancelDialog;
import nl.softworks.calendarAggregator.boundary.vdn.component.CrudButtonbar;
import nl.softworks.calendarAggregator.boundary.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.boundary.vdn.form.CalendarEventForm;
import nl.softworks.calendarAggregator.boundary.vdn.form.CalendarSourceForm;
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
@RolesAllowed({"ROLE_ADMIN", "ROLE_MAINTAIN"})
@PermitAll
public class CalendarSourceAndEventView extends CalendarAggregatorAppLayout
implements AfterNavigationObserver
{
	private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceAndEventView.class);
	private static final DateTimeFormatter YYYYMMDDHHMM = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final TreeGrid<TreeNode> calendarSourceAndEventTreeGrid = new TreeGrid<>();

	public CalendarSourceAndEventView() {
		super("Overview");
		tabs.setSelectedTab(overviewTab);

		// calendarSourceAndEventTreeGrid
		calendarSourceAndEventTreeGrid.addComponentHierarchyColumn((ValueProvider<TreeNode, Icon>) TreeNode::icon);
		calendarSourceAndEventTreeGrid.addColumn(TreeNode::text).setHeader("Name");
		calendarSourceAndEventTreeGrid.addColumn(TreeNode::startDate).setHeader("Start");
		calendarSourceAndEventTreeGrid.addColumn(TreeNode::endDate).setHeader("End");

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
		reloadTreeGrid();
	}

	private void insert() {
		TreeNode treeNode = getSelectedTreeNode();

		VerticalLayout verticalLayout = new VerticalLayout();
		CancelDialog dialog = new CancelDialog("Add", verticalLayout);

		// Manual Source
		verticalLayout.add(new Button("Manual Source", e -> {
			dialog.close();
			CalendarSourceForm.showInsertDialog(() -> reloadTreeGrid());
		}));

		// Manual Event
		if (treeNode instanceof TreeNodeCalendarSource treeNodeCalendarSource) {
			verticalLayout.add(new Button("Manual Event", e -> {
				dialog.close();
				CalendarEventForm.showInsertDialog(treeNodeCalendarSource.calendarSource(), () -> reloadTreeGrid());
			}));
		}

		dialog.open();
	}


	private void edit() {
		// Get the selected treenode
		TreeNode selectedTreeNode = getSelectedTreeNode();
		if (selectedTreeNode == null) {
			return;
		}

		// Dialog with source
		selectedTreeNode.edit(() -> reloadTreeGrid());
	}

	private void delete() {
		TreeNode selectedTreeNode = getSelectedTreeNode();
		if (selectedTreeNode == null) {
			return;
		}

		new OkCancelDialog("Remove", new NativeLabel("Are you sure?"))
				.okLabel("Yes")
				.onOk(() -> {
					selectedTreeNode.delete(() -> reloadTreeGrid());
				})
				.open();
	}


	private TreeNode getSelectedTreeNode() {
		Set<TreeNode> selectedItems = calendarSourceAndEventTreeGrid.getSelectedItems();
		if (selectedItems.isEmpty() || selectedItems.size() > 1) {
			return null;
		}
		TreeNode treeNode = selectedItems.iterator().next();
		return treeNode;
	}

	private void reloadTreeGrid() {
		// Remember selection
		TreeNode selectedTreeNode = getSelectedTreeNode();

		// Refresh data
		List<CalendarSource> calendarSources = R.calendarSource().findAll();
		List<TreeNode> treeNodes = treeNodes(calendarSources, TreeNodeCalendarSource::new);
		calendarSourceAndEventTreeGrid.setItems(treeNodes, this::getTreeNodeChildren);

		// Reselect NODE
		if (selectedTreeNode != null) {
			if (selectedTreeNode instanceof TreeNodeCalendarEvent treeNodeCalendarEvent) {
				calendarSourceAndEventTreeGrid.expand(treeNodeCalendarEvent.treeNodeCalendarSource());
			}
			calendarSourceAndEventTreeGrid.select(selectedTreeNode);
		}
	}

	sealed interface TreeNode permits TreeNodeCalendarSource, TreeNodeCalendarEvent {
		String text();
		String startDate();
		String endDate();
		Icon icon();
		void edit(Runnable onOk);
		void delete(Runnable onOk);
	}

	record TreeNodeCalendarSource(CalendarSource calendarSource) implements TreeNode {
		@Override
		public String text() {
			return calendarSource().name();
		}

		@Override
		public String startDate() {
			return null;
		}

		@Override
		public String endDate() {
			return null;
		}

		@Override
		public Icon icon() {
			return VaadinIcon.DATABASE.create();
		}

		@Override
		public void edit(Runnable onOk) {
			CalendarSourceForm calendarSourceForm = new CalendarSourceForm().populateWith(calendarSource);
			new OkCancelDialog("Source", calendarSourceForm)
					.okLabel("Save")
					.onOk(() -> {
						calendarSourceForm.writeTo(calendarSource);
						R.calendarSource().save(calendarSource);
						onOk.run();
					})
					.open();
		}

		@Override
		public void delete(Runnable onOk) {
			R.calendarSource().delete(calendarSource);
			onOk.run();
		}
	}
	record TreeNodeCalendarEvent (TreeNodeCalendarSource treeNodeCalendarSource, CalendarEvent calendarEvent) implements TreeNode {
		@Override
		public String text() {
			return calendarEvent.subject();
		}

		@Override
		public String startDate() {
			return calendarEvent.startDateTime().format(YYYYMMDDHHMM);
		}

		@Override
		public String endDate() {
			return calendarEvent.endDateTime().format(YYYYMMDDHHMM);
		}

		@Override
		public Icon icon() {
			return calendarEvent.getClass().equals(CalendarEvent.class) ? LumoIcon.EDIT.create() : null;
		}

		@Override
		public void edit(Runnable onOk) {
			CalendarEventForm calendarEventForm = new CalendarEventForm().populateWith(calendarEvent);
			new OkCancelDialog("Event", calendarEventForm)
					.okLabel("Save")
					.onOk(() -> {
						calendarEventForm.writeTo(calendarEvent);
						R.calendarEvent().save(calendarEvent);
						onOk.run();
					})
					.open();
		}

		@Override
		public void delete(Runnable onOk) {
			R.calendarEvent().delete(calendarEvent);
			onOk.run();
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