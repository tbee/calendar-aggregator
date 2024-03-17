package nl.softworks.calendarAggregator.application.vdn.view;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;
import jakarta.annotation.security.RolesAllowed;
import nl.softworks.calendarAggregator.application.vdn.CalendarAggregatorAppLayout;
import nl.softworks.calendarAggregator.application.vdn.component.CancelDialog;
import nl.softworks.calendarAggregator.application.vdn.component.CrudButtonbar;
import nl.softworks.calendarAggregator.application.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.application.vdn.component.VButton;
import nl.softworks.calendarAggregator.application.vdn.form.CalendarLocationForm;
import nl.softworks.calendarAggregator.application.vdn.form.CalendarSourceForm;
import nl.softworks.calendarAggregator.application.vdn.form.CalendarSourceICalForm;
import nl.softworks.calendarAggregator.application.vdn.form.CalendarSourceManualForm;
import nl.softworks.calendarAggregator.application.vdn.form.CalendarSourceMultipleDaysScraperForm;
import nl.softworks.calendarAggregator.application.vdn.form.CalendarSourceRegexScraperForm;
import nl.softworks.calendarAggregator.application.vdn.form.CalendarSourceXmlScraperForm;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarLocation;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceICal;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceManual;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceMultipleDaysScraper;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceRegexScraper;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceXmlScraper;
import nl.softworks.calendarAggregator.domain.service.CalendarSourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Route("/")
@StyleSheet("context://../vaadin.css")
@RolesAllowed({"ROLE_ADMIN", "ROLE_USER"})
public class CalendarLocationAndSourceView extends CalendarAggregatorAppLayout
implements AfterNavigationObserver
{
	private static final Logger LOG = LoggerFactory.getLogger(CalendarLocationAndSourceView.class);
	private static final DateTimeFormatter YYYYMMDDHHMM = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final TreeGrid<TreeNode> treeGrid = new TreeGrid<>();

	@Autowired
	private CalendarSourceService calendarSourceService;

	public CalendarLocationAndSourceView() {
		super("Overview");
		tabs.setSelectedTab(overviewTab);

		// calendarSourceAndEventTreeGrid
		treeGrid.addHierarchyColumn(TreeNode::text).setHeader("Name").setFlexGrow(100);
		treeGrid.addComponentColumn((ValueProvider<TreeNode, Button>) tn -> createEditButton(tn)).setHeader("").setFlexGrow(30);
		treeGrid.addComponentColumn((ValueProvider<TreeNode, NativeLabel>) tn -> createTypeLabel(tn)).setHeader("Type").setFlexGrow(10);
		treeGrid.addComponentColumn((ValueProvider<TreeNode, Icon>) tn -> createEnabledIcon(tn)).setHeader("Enabled").setFlexGrow(5);
		treeGrid.addComponentColumn((ValueProvider<TreeNode, Anchor>) tn -> createAnchor(tn.url())).setHeader("Website").setFlexGrow(5);
		treeGrid.addColumn(TreeNode::startDate).setHeader("Start").setFlexGrow(50);
		treeGrid.addColumn(TreeNode::endDate).setHeader("End").setFlexGrow(50);
		treeGrid.addComponentColumn((ValueProvider<TreeNode, Button>) tn -> createShowLogButton(tn)).setHeader("Status").setFlexGrow(30);
		treeGrid.addColumn(TreeNode::updated).setHeader("Updated").setFlexGrow(30);
		treeGrid.addColumn(TreeNode::childrenCount).setHeader("Children").setFlexGrow(10);
		treeGrid.addItemDoubleClickListener(e -> edit());

		// buttonbar
		CrudButtonbar crudButtonbar = new CrudButtonbar()
				.onReload(this::reloadTreeGrid)
				.onInsert(this::insert)
				.onEdit(this::edit)
				.onDelete(this::delete);
		crudButtonbar.add(new Button("Generate", (ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> generate()));

		// content
		VerticalLayout verticalLayout = new VerticalLayout(crudButtonbar, treeGrid);
		verticalLayout.setSizeFull();
		setContent(verticalLayout);
	}

	private Button createEditButton(TreeNode treeNode) {
		Button button = new Button(LumoIcon.EDIT.create(), evt -> treeNode.edit(() -> reloadTreeGrid()));
		button.addThemeVariants(ButtonVariant.LUMO_SMALL);
		return button;
	}

	private NativeLabel createTypeLabel(TreeNode tn) {
		NativeLabel nativeLabel = new NativeLabel(tn.type());
		Tooltip.forComponent(nativeLabel)
				.withText(tn.hint())
				.withPosition(Tooltip.TooltipPosition.TOP_START);
		return nativeLabel;
	}

	private Icon createEnabledIcon(TreeNode tn) {
		return tn.enabled() == null ? null : tn.enabled() ? VaadinIcon.CHECK.create() : VaadinIcon.MINUS.create();
	}

	private Anchor createAnchor(String url) {
		if (url == null) {
			return null;
		}
		Anchor anchor = new Anchor(url, "⇒");
		anchor.setTarget("_blank");
		return anchor;
	}

	private Button createShowLogButton(TreeNode treeNode) {
		Button button = new Button(treeNode.status(), evt -> showLogInDialog(treeNode.calendarSource()));
		button.setEnabled(treeNode.calendarSource() != null);
		button.addThemeVariants(ButtonVariant.LUMO_SMALL);
		return button;
	}

	private void showLogInDialog(CalendarSource calendarSource) {
		if (calendarSource == null) {
			return;
		}
		TextArea textArea = new TextArea("Result", calendarSource.log(), "");
		textArea.setSizeFull();

		CancelDialog cancelDialog = new CancelDialog("Log", textArea);
		cancelDialog.setSizeFull();
		cancelDialog.open();
	}

	private void generate() {
		calendarSourceService.generateEvents(() -> {
			reloadTreeGrid();
			showSuccessNotification("Generating");
		});
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		reloadTreeGrid();
	}

	private void insert() {
		TreeNode treeNode = getSelectedTreeNode();
		CalendarSource calendarSource = (treeNode == null ? null : treeNode.calendarSource());

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		CancelDialog addSelectionDialog = new CancelDialog("Add", horizontalLayout);

		horizontalLayout.add(new VerticalLayout(new VButton("Location", e -> {
			addSelectionDialog.close();
			CalendarLocationForm.showInsertDialog(null, () -> reloadTreeGrid());
		})));

		VerticalLayout verticalLayout = new VerticalLayout();
		horizontalLayout.add(verticalLayout);

		verticalLayout.add(new VButton("Manual Source", e -> {
			addSelectionDialog.close();
			CalendarSourceManualForm.showInsertDialog(calendarSource, () -> reloadTreeGrid());
		}).withIsPrimary(calendarSource != null));

		verticalLayout.add(new VButton("Regex Source", e -> {
			addSelectionDialog.close();
			CalendarSourceRegexScraper calendarSourceRegexScraper = (calendarSource instanceof CalendarSourceRegexScraper ? (CalendarSourceRegexScraper)calendarSource : null);
			CalendarSourceRegexScraperForm.showInsertDialog(calendarSourceRegexScraper, () -> reloadTreeGrid());
		}).withIsPrimary(calendarSource instanceof CalendarSourceRegexScraper));

		verticalLayout.add(new VButton("Multiple days Source", e -> {
			addSelectionDialog.close();
			CalendarSourceMultipleDaysScraper calendarSourceMultipleDaysScraper = (calendarSource instanceof CalendarSourceMultipleDaysScraper ? (CalendarSourceMultipleDaysScraper)calendarSource : null);
			CalendarSourceMultipleDaysScraperForm.showInsertDialog(calendarSourceMultipleDaysScraper, () -> reloadTreeGrid());
		}).withIsPrimary(calendarSource instanceof CalendarSourceMultipleDaysScraper));

		verticalLayout.add(new VButton("ICal Source", e -> {
			addSelectionDialog.close();
			CalendarSourceICal calendarSourceICal = (calendarSource instanceof CalendarSourceICal ? (CalendarSourceICal)calendarSource : null);
			CalendarSourceICalForm.showInsertDialog(calendarSourceICal, () -> reloadTreeGrid());
		}).withIsPrimary(calendarSource instanceof CalendarSourceICal));

		verticalLayout.add(new VButton("XML/JSON Source", e -> {
			addSelectionDialog.close();
			CalendarSourceXmlScraper calendarSourceXmlScraper = (calendarSource instanceof CalendarSourceXmlScraper ? (CalendarSourceXmlScraper)calendarSource : null);
			CalendarSourceXmlScraperForm.showInsertDialog(calendarSourceXmlScraper, () -> reloadTreeGrid());
		}).withIsPrimary(calendarSource instanceof CalendarSourceXmlScraper));

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
		Set<TreeNode> selectedItems = treeGrid.getSelectedItems();
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
		List<CalendarLocation> calendarLocations = R.calendarLocation().findAll();
		// not ok statusses should always come first
		Comparator<CalendarLocation> compareByStatus = (cl1, cl2) -> {
			boolean cs1ok = cl1.statusIsOk();
			boolean cs2ok = cl2.statusIsOk();
			if ((cs1ok && cs2ok) || (!cs1ok && !cs2ok)) {
				return 0;
			}
			if (!cs1ok) {
				return -1;
			}
			return 1;
		};
		Comparator<CalendarLocation> compareByName = Comparator.comparing(CalendarLocation::name, String.CASE_INSENSITIVE_ORDER);
		calendarLocations.sort(compareByStatus.thenComparing(compareByName));
		List<TreeNode> treeNodes = treeNodes(calendarLocations, TreeNodeCalendarLocation::new);
		treeGrid.setItems(treeNodes, this::getTreeNodeChildren);

		// Reselect NODE
		// TODO: make sure the select node is one from the treeNodes collection, not the old node, otherwise lazy lock goes wrong
//		if (selectedTreeNode != null) {
//			if (selectedTreeNode instanceof TreeNodeCalendarEvent treeNodeCalendarEvent) {
//				calendarSourceAndEventTreeGrid.expand(treeNodeCalendarEvent.treeNodeCalendarSource());
//			}
//			calendarSourceAndEventTreeGrid.select(selectedTreeNode);
//		}
	}

	sealed interface TreeNode permits TreeNodeCalendarLocation, TreeNodeCalendarSource {
		String text();
		Boolean enabled();
		String type();
		String url();
		String startDate();
		String endDate();
		CalendarSource calendarSource();
		void edit(Runnable onOk);
		void delete(Runnable onOk);

		String status();
		LocalDateTime updated();

		int childrenCount();

		String hint();
	}

	record TreeNodeCalendarLocation(CalendarLocation calendarLocation) implements TreeNode {
		@Override
		public String text() {
			return calendarLocation.name();
		}
		@Override
		public Boolean enabled() {
			return calendarLocation.enabled();
		}

		@Override
		public String type() {
			long count = calendarLocation.calendarSources().stream().map(cs -> cs.type()).distinct().count();
			return count == 0 ? "" : (count > 1 ? "Mix" : calendarLocation.calendarSources().get(0).type());
		}
		@Override
		public String url() {
			return calendarLocation.url();
		}

		@Override
		public String startDate() {
			return "";
		}

		@Override
		public String endDate() {
			return "";
		}

		@Override
		public CalendarSource calendarSource() {
			return null;
		}

		@Override
		public void edit(Runnable onOk) {
			CalendarLocationForm calendarLocationForm = new CalendarLocationForm().populateWith(calendarLocation);
			new OkCancelDialog("Location", calendarLocationForm)
					.okLabel("Save")
					.onOk(() -> {
						try {
							calendarLocationForm.writeTo(calendarLocation);
							R.calendarLocation().save(calendarLocation);
							onOk.run();
						} catch (ValidationException e) {
							throw new RuntimeException(e);
						}
					})
					.open();
		}

		@Override
		public void delete(Runnable onOk) {
			R.calendarLocation().delete(calendarLocation);
			onOk.run();
		}

		@Override
		public String status() {
			return calendarLocation().status();
		}

		@Override
		public LocalDateTime updated() {
			return null;
		}

		@Override
		public int childrenCount() {
			return calendarLocation.calendarSources().size();
		}

		@Override
		public String hint() {
			return "";
		}
	}

	record TreeNodeCalendarSource(TreeNodeCalendarLocation treeNodeCalendarSource, CalendarSource calendarSource) implements TreeNode {
		@Override
		public String text() {
			return calendarSource.description() == null || calendarSource.description().isBlank() ? hint() : calendarSource.description();
		}
		@Override
		public Boolean enabled() {
			return calendarSource.enabled();
		}
		@Override
		public String type() {
			return calendarSource().type();
		}
		@Override
		public String url() {
			return null;
		}

		@Override
		public String startDate() {
			if (calendarSource instanceof CalendarSourceManual calendarSourceManual) {
				return calendarSourceManual.startDateTime().toString();
			}
			return null;
		}

		@Override
		public String endDate() {
			if (calendarSource instanceof CalendarSourceManual calendarSourceManual) {
				return calendarSourceManual.endDateTime().toString();
			}
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
			else if (calendarSource instanceof CalendarSourceManual) {
				calendarSourceForm = new CalendarSourceManualForm().populateWith(calendarSource);
				title = "Manual source";
			}
			else if (calendarSource instanceof CalendarSourceMultipleDaysScraper) {
				calendarSourceForm = new CalendarSourceMultipleDaysScraperForm().populateWith(calendarSource);
				title = "Multidays source";
			}
			else if (calendarSource instanceof CalendarSourceICal) {
				calendarSourceForm = new CalendarSourceICalForm().populateWith(calendarSource);
				title = "ICal source";
			}
			else if (calendarSource instanceof CalendarSourceXmlScraper) {
				calendarSourceForm = new CalendarSourceXmlScraperForm().populateWith(calendarSource);
				title = "XML/JSON source";
			}
			else {
				throw new IllegalStateException("Unknown CalendarSource " + calendarSource.getClass().getSimpleName());
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
		public LocalDateTime updated() {
			return calendarSource.lastRun();
		}

		@Override
		public int childrenCount() {
			return calendarSource.calendarEvents().size();
		}

		@Override
		public String hint() {
			if (calendarSource instanceof CalendarSourceRegexScraper calendarSourceRegexScraper) {
				return calendarSourceRegexScraper.regex();
			}
			if (calendarSource instanceof CalendarSourceXmlScraper calendarSourceXmlScraper) {
				return calendarSourceXmlScraper.xpath();
			}
			if (calendarSource instanceof CalendarSourceManual calendarSourceManual) {
				return calendarSourceManual.startDateTime().toString() + " " + (calendarSourceManual.rrule() == null ? "" : calendarSourceManual.rrule());
			}
			return "";
		}
	}

	public List<TreeNode> getTreeNodeChildren(TreeNode treeNode) {
		if (treeNode instanceof TreeNodeCalendarLocation treeNodeCalendarLocation) {
			List<CalendarSource> calendarSources = new ArrayList<>(treeNodeCalendarLocation.calendarLocation().calendarSources());
//			calendarSources.sort(Comparator.comparing(CalendarSource::name));
			return treeNodes(calendarSources, ce -> new TreeNodeCalendarSource(treeNodeCalendarLocation, ce));
		}
		return List.of();
	}

	private <T> List<TreeNode> treeNodes(Collection<T> businessObjects, Function<T, TreeNode> converter) {
		List<TreeNode> treeNodes = new ArrayList<>();
		businessObjects.stream().map(bo -> converter.apply(bo)).forEach(tn -> treeNodes.add(tn));
		return treeNodes;
	}
}