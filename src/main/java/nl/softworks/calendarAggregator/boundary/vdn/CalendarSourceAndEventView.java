package nl.softworks.calendarAggregator.boundary.vdn;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTargetValue;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import nl.softworks.calendarAggregator.boundary.vdn.component.CancelDialog;
import nl.softworks.calendarAggregator.boundary.vdn.component.CrudButtonbar;
import nl.softworks.calendarAggregator.boundary.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.boundary.vdn.form.CalendarEventForm;
import nl.softworks.calendarAggregator.boundary.vdn.form.CalendarSourceForm;
import nl.softworks.calendarAggregator.boundary.vdn.form.CalendarSourceMultipleDaysScraperForm;
import nl.softworks.calendarAggregator.boundary.vdn.form.CalendarSourceRegexScraperForm;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceMultipleDaysScraper;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceRegexScraper;
import nl.softworks.calendarAggregator.domain.service.CalendarSourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
	private List<CalendarSource> calendarSources = List.of();

	@Autowired
	private CalendarSourceService calendarSourceService;

	public CalendarSourceAndEventView() {
		super("Overview");
		tabs.setSelectedTab(overviewTab);

		// calendarSourceAndEventTreeGrid
		calendarSourceAndEventTreeGrid.addHierarchyColumn(TreeNode::text).setHeader("Name");
		calendarSourceAndEventTreeGrid.addComponentColumn((ValueProvider<TreeNode, Anchor>) tn -> createAnchor(tn.url())).setHeader("Website");
		calendarSourceAndEventTreeGrid.addColumn(TreeNode::type).setHeader("Type");
		calendarSourceAndEventTreeGrid.addColumn(TreeNode::startDate).setHeader("Start");
		calendarSourceAndEventTreeGrid.addColumn(TreeNode::endDate).setHeader("End");
		calendarSourceAndEventTreeGrid.addColumn(TreeNode::status).setHeader("Status");
		calendarSourceAndEventTreeGrid.addColumn(TreeNode::eventCount).setHeader("Events");
		calendarSourceAndEventTreeGrid.addItemDoubleClickListener(e -> edit());

		// buttonbar
		CrudButtonbar crudButtonbar = new CrudButtonbar()
				.onReload(this::reloadTreeGrid)
				.onInsert(this::insert)
				.onEdit(this::edit)
				.onDelete(this::delete);
		crudButtonbar.add(new Button("Generate", (ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> generate()));

		// content
		VerticalLayout verticalLayout = new VerticalLayout(crudButtonbar, calendarSourceAndEventTreeGrid);
		verticalLayout.setSizeFull();
		setContent(verticalLayout);
	}

	private Anchor createAnchor(String url) {
		Anchor anchor = new Anchor(url, "⇒");
		anchor.setTarget("_blank");
		return anchor;
	}

	private void generate() {
		calendarSourceService.generateEvents(() -> {
			reloadTreeGrid();
			showSuccessNotification("Generated");
		});
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		reloadTreeGrid();
	}

	private void insert() {
		TreeNode treeNode = getSelectedTreeNode();
		CalendarSource calendarSource = (treeNode == null ? null : treeNode.calendarSource());

		VerticalLayout verticalLayout = new VerticalLayout();
		CancelDialog addSelectionDialog = new CancelDialog("Add", verticalLayout);

		verticalLayout.add(new Button("Manual Source", e -> {
			addSelectionDialog.close();
			CalendarSourceForm.showInsertDialog(calendarSource, () -> reloadTreeGrid());
		}));

		verticalLayout.add(new Button("Regex Source", e -> {
			addSelectionDialog.close();
			CalendarSourceRegexScraper calendarSourceRegexScraper = (calendarSource instanceof CalendarSourceRegexScraper ? (CalendarSourceRegexScraper)calendarSource : null);
			CalendarSourceRegexScraperForm.showInsertDialog(calendarSourceRegexScraper, () -> reloadTreeGrid());
		}));

		verticalLayout.add(new Button("Multiple days Source", e -> {
			addSelectionDialog.close();
			CalendarSourceMultipleDaysScraper calendarSourceMultipleDaysScraper = (calendarSource instanceof CalendarSourceMultipleDaysScraper ? (CalendarSourceMultipleDaysScraper)calendarSource : null);
			CalendarSourceMultipleDaysScraperForm.showInsertDialog(calendarSourceMultipleDaysScraper, () -> reloadTreeGrid());
		}));

		verticalLayout.add(new Button("Manual Event", e -> {
			addSelectionDialog.close();
			CalendarEventForm.showInsertDialog(calendarSource, () -> reloadTreeGrid());
		}));

		addSelectionDialog.open();
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

		new OkCancelDialog("Remove " + selectedTreeNode.text(), new NativeLabel("Are you sure?"))
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
		calendarSources = R.calendarSource().findAll();
		calendarSources.sort(Comparator.comparing(CalendarSource::name));
		List<TreeNode> treeNodes = treeNodes(calendarSources, TreeNodeCalendarSource::new);
		calendarSourceAndEventTreeGrid.setItems(treeNodes, this::getTreeNodeChildren);

		// Reselect NODE
		// TODO: make sure the select node is one from the treeNodes collection, not the old node, otherwise lazy lock goes wrong
//		if (selectedTreeNode != null) {
//			if (selectedTreeNode instanceof TreeNodeCalendarEvent treeNodeCalendarEvent) {
//				calendarSourceAndEventTreeGrid.expand(treeNodeCalendarEvent.treeNodeCalendarSource());
//			}
//			calendarSourceAndEventTreeGrid.select(selectedTreeNode);
//		}
	}

	sealed interface TreeNode permits TreeNodeCalendarSource, TreeNodeCalendarEvent {
		String text();
		String type();
		String url();
		String startDate();
		String endDate();
		CalendarSource calendarSource();
		void edit(Runnable onOk);
		void delete(Runnable onOk);

		String status();

		int eventCount();

	}

	record TreeNodeCalendarSource(CalendarSource calendarSource) implements TreeNode {
		@Override
		public String text() {
			return calendarSource().name();
		}

		@Override
		public String type() {
			return calendarSource().type();
		}
		@Override
		public String url() {
			return calendarSource().url();
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
		public void edit(Runnable onOk) {
			final CalendarSourceForm calendarSourceForm;
			final String title;
			if (calendarSource instanceof CalendarSourceRegexScraper) {
				calendarSourceForm = new CalendarSourceRegexScraperForm().populateWith(calendarSource);
				title = "Regexp source";
			}
			else if (calendarSource instanceof CalendarSourceMultipleDaysScraper) {
				calendarSourceForm = new CalendarSourceMultipleDaysScraperForm().populateWith(calendarSource);
				title = "Multidays source";
			}
			else {
				calendarSourceForm = new CalendarSourceForm().populateWith(calendarSource);
				title = "Manual source";
			}
			new OkCancelDialog(title, calendarSourceForm)
					.okLabel("Save")
					.onOk(() -> {
						try {
							calendarSourceForm.writeTo(calendarSource);
							R.calendarSource().save(calendarSource);
							onOk.run();
						} catch (ValidationException e) {
							throw new RuntimeException(e);
						}
					})
					.open();
		}

		@Override
		public void delete(Runnable onOk) {
			R.calendarSource().delete(calendarSource);
			onOk.run();
		}

		@Override
		public String status() {
			return calendarSource.status();
		}

		@Override
		public int eventCount() {
			return calendarSource.calendarEvents().size();
		}
	}

	record TreeNodeCalendarEvent(TreeNodeCalendarSource treeNodeCalendarSource, CalendarEvent calendarEvent) implements TreeNode {
		@Override
		public String text() {
			return calendarEvent.subject();
		}

		@Override
		public String type() {
			return "";
		}
		@Override
		public String url() {
			return treeNodeCalendarSource.url();
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
		public CalendarSource calendarSource() {
			return null;
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

		@Override
		public String status() {
			return "";
		}

		@Override
		public int eventCount() {
			return calendarEvent.generated() ? 0 : 1;
		}
	}

	public List<TreeNode> getTreeNodeChildren(TreeNode treeNode) {
		if (treeNode instanceof TreeNodeCalendarSource treeNodeCalendarSource) {
			return treeNodes(treeNodeCalendarSource.calendarSource().calendarEvents(), ce -> new TreeNodeCalendarEvent(treeNodeCalendarSource, ce));
		}
		return List.of();
	}

	private <T> List<TreeNode> treeNodes(Collection<T> businessObjects, Function<T, TreeNode> converter) {
		List<TreeNode> treeNodes = new ArrayList<>();
		businessObjects.stream().map(bo -> converter.apply(bo)).forEach(tn -> treeNodes.add(tn));
		return treeNodes;
	}
}