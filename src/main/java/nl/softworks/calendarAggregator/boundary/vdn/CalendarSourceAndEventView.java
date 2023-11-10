package nl.softworks.calendarAggregator.boundary.vdn;

import com.vaadin.flow.component.dependency.StyleSheet;
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
import nl.softworks.calendarAggregator.boundary.vdn.component.CrudButtonbar;
import nl.softworks.calendarAggregator.boundary.vdn.component.OkCancelDialog;
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
public class CalendarSourceAndEventView extends CalendarAggregatorAppLayout
implements AfterNavigationObserver
{
	private static final Logger LOG = LoggerFactory.getLogger(CalendarSourceAndEventView.class);
	private static final DateTimeFormatter YYYYMMDDHHMM = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final TreeGrid<TreeNode> calendarSourceAndEventTreeGrid = new TreeGrid<>();
	private List<TreeNode> treeNodes = null;

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

		// Dialog with source
		if (treeNode instanceof TreeNodeCalendarSource treeNodeCalendarSource) {
			CalendarSource calendarSource = treeNodeCalendarSource.calendarSource();
			CalendarSourceForm calendarSourceForm = new CalendarSourceForm().populateWith(calendarSource);
			new OkCancelDialog("Source", calendarSourceForm)
					.okLabel("Save")
					.onOk(() -> {
						calendarSourceForm.writeTo(calendarSource);
						R.calendarSource().save(calendarSource);
						refreshTreeGrid();
					})
					.open();
		}

		// Dialog with event
		if (treeNode instanceof TreeNodeCalendarEvent treeNodeCalendarEvent) {
			CalendarEvent calendarEvent = treeNodeCalendarEvent.calendarEvent();
			CalendarEventForm calendarEventForm = new CalendarEventForm().populateWith(calendarEvent);
			new OkCancelDialog("Event", calendarEventForm)
					.okLabel("Save")
					.onOk(() -> {
						calendarEventForm.writeTo(calendarEvent);
						R.calendarEvent().save(calendarEvent);
						refreshTreeGrid();
					})
					.open();
		}
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
		String text();
		String startDate();
		String endDate();
		Icon icon();
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