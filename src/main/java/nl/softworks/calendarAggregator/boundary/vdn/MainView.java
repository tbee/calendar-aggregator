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

	private final TreeGrid<TreeNode> treeGrid = new TreeGrid<>();
	private final CalendarSourceForm calendarSourceForm = new CalendarSourceForm();
	private final Dialog calendarSourceDialog = new Dialog("Source", calendarSourceForm);
	private final CalendarEventForm calendarEventForm = new CalendarEventForm();
	private final Dialog calendarEventDialog = new Dialog("Event", calendarEventForm);
	private List<TreeNode> treeNodes = null;

	public MainView() {
		super("Overview");
		tabs.setSelectedTab(overviewTab);

		// treeGrid
		treeGrid.addHierarchyColumn(TreeNode::getText).setHeader("Name");
		treeGrid.addColumn(TreeNode::getStartDate).setHeader("Start");
		treeGrid.addColumn(TreeNode::getEndDate).setHeader("End");

		// buttonbar
		Button insertButton = new Button(VaadinIcon.PLUS.create());
		insertButton.addClickListener(buttonClickEvent -> insert());
		Button editButton = new Button(VaadinIcon.EDIT.create());
		editButton.addClickListener(buttonClickEvent -> edit());
		Button deleteButton = new Button(VaadinIcon.TRASH.create());
		deleteButton.addClickListener(buttonClickEvent -> delete());
		HorizontalLayout buttonbar = new HorizontalLayout(insertButton, editButton, deleteButton);

		// calendarSourceDialog
		setupDialog(calendarSourceDialog);

		// calendarSourceDialog
		setupDialog(calendarEventDialog);

		// content
		setContent(new VerticalLayout(buttonbar, treeGrid, calendarSourceDialog));
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		List<CalendarSource> calendarSources = R.calendarSource().findAll();
		treeNodes = treeNodes(calendarSources, TreeNodeCalendarSource::new);
		treeGrid.setItems(treeNodes, this::getTreeNodeChildren);
	}

	private <T> void setupDialog(Dialog dialog) {
		Button closeButton = new Button(LumoIcon.CROSS.create(), e -> dialog.close());
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		dialog.getHeader().add(closeButton);
		Button saveButton = new Button("Save", VaadinIcon.SAFE.create(), e -> save());
		dialog.getFooter().add(saveButton);
	}

	private void insert() {
	}

	private void edit() {
		Set<TreeNode> selectedItems = treeGrid.getSelectedItems();
		if (selectedItems.isEmpty() || selectedItems.size() > 1) {
			return;
		}
		TreeNode treeNode = selectedItems.iterator().next();

		if (treeNode instanceof TreeNodeCalendarSource treeNodeCalendarSource) {
			calendarSourceForm.populateWith(treeNodeCalendarSource.calendarSource());
			calendarSourceDialog.open();

		}
		if (treeNode instanceof TreeNodeCalendarEvent treeNodeCalendarEvent) {
			calendarEventForm.populateWith(treeNodeCalendarEvent.calendarEvent());
			calendarEventDialog.open();
		}
	}

	private void delete() {
	}

	private void save() {
		Set<TreeNode> selectedItems = treeGrid.getSelectedItems();
		if (selectedItems.isEmpty() || selectedItems.size() > 1) {
			return;
		}
		TreeNode treeNode = selectedItems.iterator().next();

		if (treeNode instanceof TreeNodeCalendarSource treeNodeCalendarSource) {
			calendarSourceForm.writeTo(treeNodeCalendarSource.calendarSource());
			R.calendarSource().save(treeNodeCalendarSource.calendarSource());
			calendarSourceDialog.close();
		}
		if (treeNode instanceof TreeNodeCalendarEvent treeNodeCalendarEvent) {
			calendarEventForm.writeTo(treeNodeCalendarEvent.calendarEvent());
			R.calendarEvent().save(treeNodeCalendarEvent.calendarEvent());
			calendarEventDialog.close();
		}
		treeGrid.setItems(treeNodes, this::getTreeNodeChildren);
        if (treeNode instanceof TreeNodeCalendarEvent treeNodeCalendarEvent) {
            treeGrid.expand(treeNodeCalendarEvent.treeNodeCalendarSource());
        }
		treeGrid.select(treeNode);
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