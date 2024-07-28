package nl.softworks.calendarAggregator.application.vdn.view;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;
import jakarta.annotation.security.RolesAllowed;
import nl.softworks.calendarAggregator.application.vdn.CalendarAggregatorAppLayout;
import nl.softworks.calendarAggregator.application.vdn.component.CancelDialog;
import nl.softworks.calendarAggregator.application.vdn.component.CrudButtonbar;
import nl.softworks.calendarAggregator.application.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.application.vdn.component.ResultDialog;
import nl.softworks.calendarAggregator.application.vdn.component.VButton;
import nl.softworks.calendarAggregator.application.vdn.form.CalendarLocationForm;
import nl.softworks.calendarAggregator.application.vdn.form.CalendarSourceForm;
import nl.softworks.calendarAggregator.application.vdn.form.CalendarSourceICalForm;
import nl.softworks.calendarAggregator.application.vdn.form.CalendarSourceManualForm;
import nl.softworks.calendarAggregator.application.vdn.form.CalendarSourceMultipleDaysScraperForm;
import nl.softworks.calendarAggregator.application.vdn.form.CalendarSourceRegexScraperForm;
import nl.softworks.calendarAggregator.application.vdn.form.CalendarSourceXmlScraperForm;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarLocation;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceICal;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceManual;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceMultipleDaysScraper;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceRegexScraper;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceXmlScraper;
import nl.softworks.calendarAggregator.domain.service.GenerateEventsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

@Route("/")
@StyleSheet("context://../vaadin.css")
@JavaScript("https://kit.fontawesome.com/501b8808a2.js")
@RolesAllowed({"ROLE_ADMIN", "ROLE_USER"})
public class CalendarLocationAndSourceView extends CalendarAggregatorAppLayout
implements AfterNavigationObserver
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarLocationAndSourceView.class);
	public static final LumoIcon ENABLED_ICON = LumoIcon.PLUS;
	public static final LumoIcon DISABLED_ICON = LumoIcon.CROSS;

	private final TreeGrid<TreeNode> treeGrid = new TreeGrid<>();

	@Autowired
	private GenerateEventsService generateEventsService;

	public CalendarLocationAndSourceView() {
		super("Overview");
		tabs.setSelectedTab(overviewTab);

		// calendarSourceAndEventTreeGrid
		treeGrid.addHierarchyColumn(TreeNode::text).setHeader("Name").setFlexGrow(100);
		treeGrid.addComponentColumn(TreeNode::crudButtons).setHeader("").setFlexGrow(30);
		treeGrid.addComponentColumn(TreeNode::enabled).setHeader("Enabled").setFlexGrow(5);
		treeGrid.addComponentColumn(TreeNode::url).setHeader("Website").setFlexGrow(5);
		treeGrid.addColumn(TreeNode::startDate).setHeader("Start").setFlexGrow(50);
		treeGrid.addColumn(TreeNode::endDate).setHeader("End").setFlexGrow(50);
		treeGrid.addComponentColumn(TreeNode::status).setHeader("Status").setFlexGrow(30);
		treeGrid.addColumn(TreeNode::updated).setHeader("Updated").setFlexGrow(30);
		treeGrid.addColumn(TreeNode::childrenCount).setHeader("Children").setFlexGrow(10);

		// buttonbar
		CrudButtonbar crudButtonbar = new CrudButtonbar()
				.onReload(this::reloadTreeGrid)
				.onInsert(this::insert);
		crudButtonbar.add(new Button("Generate", (ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> generate()));

		// content
		VerticalLayout verticalLayout = new VerticalLayout(crudButtonbar, treeGrid);
		verticalLayout.setSizeFull();
		setContent(verticalLayout);
	}

	private void generate() {
		generateEventsService.generateEvents(() -> {
			reloadTreeGrid();
			showSuccessNotification("Generating");
		});
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		reloadTreeGrid();
	}

	private void insert() {
		CalendarLocationForm.showInsertDialog(null, () -> reloadTreeGrid());
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
		treeGrid.setItems(treeNodes, TreeNode::getChildren);

		// Reselect NODE
		if (selectedTreeNode != null) {
			List<String> path = selectedTreeNode.path();
			for (int idx = 0; idx < path.size(); idx++) {
				int i = idx;
				TreeNode treeNode = treeNodes.stream().filter(tn -> tn.path().get(i).equals(path.get(i))).findFirst().orElse(null);
				if (treeNode == null) {
					break;
				}

				treeNodes = new ArrayList<>(treeNode.getChildren());
				if (!treeNodes.isEmpty()) {
					treeGrid.expand(treeNode);
				}
				treeGrid.select(treeNode);
			};
		}
	}

	sealed interface TreeNode permits TreeNodeCalendarLocation, TreeNodeCalendarSource, TreeNodeCalendarEvent {
		List<String> path();
		String text();
		Icon enabled();
		Component url();
		String startDate();
		String endDate();
		CalendarSource calendarSource();
		TreeNode parent();

		Component status();
		LocalDateTime updated();

		int childrenCount();

		String hint();

		Collection<TreeNode> getChildren();

		Component crudButtons();
	}

	final class TreeNodeCalendarLocation implements TreeNode {
		private final CalendarLocation calendarLocation;
		private final List<String> path;

		public TreeNodeCalendarLocation(CalendarLocation calendarLocation) {
			this.calendarLocation = calendarLocation;
			this.path = List.of("CL" + calendarLocation.id());
		}

		@Override
		public List<String> path() {
			return path;
		}
		@Override
		public boolean equals(Object o) {
			return o != null && Objects.equals(path(), ((TreeNode)o).path());
		}

		@Override
		public String text() {
			return calendarLocation.name();
		}
		@Override
		public Icon enabled() {
			return calendarLocation.isEnabled() ? null : DISABLED_ICON.create();
		}

		@Override
		public Component url() {
			return createUrlAnchor(calendarLocation.url());
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
		public TreeNode parent() {
			return null;
		}

		@Override
		public Component status() {
			return new NativeLabel(calendarLocation.status());
		}

		@Override
		public LocalDateTime updated() {
			return calendarLocation.calendarSources().stream()
					.filter(cs -> cs.isEnabled())
					.map(cs -> cs.lastRun())
					.filter(ls -> ls != null)
					.min(Comparator.naturalOrder())
					.orElse(null);
		}

		@Override
		public int childrenCount() {
			return calendarLocation.calendarSources().size();
		}

		@Override
		public String hint() {
			return "";
		}

		@Override
		public Collection<TreeNode> getChildren() {
			List<CalendarSource> calendarSources = new ArrayList<>(calendarLocation.calendarSources());
			calendarSources.sort(Comparator.comparing(CalendarSource::description));
			return treeNodes(calendarSources, ce -> new TreeNodeCalendarSource(this, ce));
		}

		@Override
		public Component crudButtons() {
			return new CrudButtonbar()
					.onInsert(this::insert)
					.onEdit(this::edit)
					.onDelete(() -> delete());
		}

		private void delete() {
			treeGrid.select(this); // for reselect after reload
			confirmDelete(this, () -> R.calendarLocation().delete(calendarLocation));
		}

		private void insert() {
			TreeNode selectedTreeNode = getSelectedTreeNode();
			CalendarSource calendarSource = selectedTreeNode == null ? null : selectedTreeNode.calendarSource(); // default

			treeGrid.select(this); // for reselect after reload

			VerticalLayout verticalLayout = new VerticalLayout();
			CancelDialog addSelectionDialog = new CancelDialog("Add", verticalLayout);

			verticalLayout.add(new VButton("Manual Source", e -> showInsertForm(addSelectionDialog, new CalendarSourceManual(), new CalendarSourceManualForm(), calendarSource))
					.withIsPrimary(calendarSource != null));

			verticalLayout.add(new VButton("Regex Source", e -> showInsertForm(addSelectionDialog, new CalendarSourceRegexScraper(), new CalendarSourceRegexScraperForm(), calendarSource))
					.withIsPrimary(calendarSource instanceof CalendarSourceRegexScraper));

			verticalLayout.add(new VButton("Multiple days Source", e -> showInsertForm(addSelectionDialog, new CalendarSourceMultipleDaysScraper(), new CalendarSourceMultipleDaysScraperForm(), calendarSource))
					.withIsPrimary(calendarSource instanceof CalendarSourceMultipleDaysScraper));

			verticalLayout.add(new VButton("ICal Source", e -> showInsertForm(addSelectionDialog, new CalendarSourceICal(), new CalendarSourceICalForm(), calendarSource))
					.withIsPrimary(calendarSource instanceof CalendarSourceICal));

			verticalLayout.add(new VButton("XML/JSON Source", e -> showInsertForm(addSelectionDialog, new CalendarSourceXmlScraper(), new CalendarSourceXmlScraperForm(), calendarSource))
					.withIsPrimary(calendarSource instanceof CalendarSourceXmlScraper));

			addSelectionDialog.open();
		}
		private void showInsertForm(CancelDialog addSelectionDialog, CalendarSource calendarSource, CalendarSourceForm calendarSourceForm, CalendarSource calendarSourceDefault) {
			addSelectionDialog.close();
			CalendarLocationAndSourceView.this.showInsertForm(calendarLocation, calendarSource, calendarSourceForm, calendarSourceDefault);
		}

		private void edit() {
			treeGrid.select(this); // for reselect after reload
			CalendarLocationForm calendarLocationForm = new CalendarLocationForm().populateWith(calendarLocation);
			showEditForm("Location", calendarLocationForm, () -> {
				calendarLocationForm.writeTo(calendarLocation);
				R.calendarLocation().save(calendarLocation);
				return null;
			});
		}
	}

	final class TreeNodeCalendarSource implements TreeNode {

		private final TreeNodeCalendarLocation treeNodeCalendarLocation;
		private final CalendarSource calendarSource;
		private final List<String> path;

		public TreeNodeCalendarSource(TreeNodeCalendarLocation treeNodeCalendarLocation, CalendarSource calendarSource) {
			this.treeNodeCalendarLocation = treeNodeCalendarLocation;
			this.calendarSource = calendarSource;
			this.path = Lists.newArrayList(Iterables.concat(treeNodeCalendarLocation.path(), List.of("CS" + calendarSource.id())));
		}

		@Override
		public List<String> path() {
			return path;
		}
		@Override
		public boolean equals(Object o) {
			return o != null && Objects.equals(path(), ((TreeNode)o).path());
		}

		@Override
		public String text() {
			return calendarSource.type() + ": " + (calendarSource.description() == null || calendarSource.description().isBlank() ? hint() : calendarSource.description());
		}
		@Override
		public Icon enabled() {
			Icon icon = calendarSource.isEnabled() ? ENABLED_ICON.create() : DISABLED_ICON.create();
			Tooltip.forComponent(icon)
					.withText(hint())
					.withPosition(Tooltip.TooltipPosition.TOP_START);
			return icon;
		}
		@Override
		public Component url() {
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
		public CalendarSource calendarSource() {
			return calendarSource;
		}
		@Override
		public TreeNode parent() {
			return treeNodeCalendarLocation;
		}

		@Override
		public Component status() {
			Button button = new Button(calendarSource.status(), evt -> new ResultDialog(calendarSource.log()).open());
			button.addThemeVariants(ButtonVariant.LUMO_SMALL);
			return button;
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
			else if (calendarSource instanceof CalendarSourceICal calendarSourceICal) {
				return calendarSourceICal.regex();
			}
			else if (calendarSource instanceof CalendarSourceXmlScraper calendarSourceXmlScraper) {
				return calendarSourceXmlScraper.xpath();
			}
			else if (calendarSource instanceof CalendarSourceManual calendarSourceManual) {
				return calendarSourceManual.startDateTime().toString() + " " + (calendarSourceManual.rrule() == null ? "" : calendarSourceManual.rrule());
			}
			return "";
		}

		@Override
		public Collection<TreeNode> getChildren() {
			List<CalendarEvent> calendarEvents = new ArrayList<>(calendarSource.calendarEvents());
			calendarEvents.sort(Comparator.comparing(CalendarEvent::startDateTime));
			return treeNodes(calendarEvents, ce -> new TreeNodeCalendarEvent(this, ce));
		}

		@Override
		public Component crudButtons() {
			return new CrudButtonbar()
					.onEdit(this::edit)
					.onDelete(this::delete);
		}

		private void delete() {
			treeGrid.select(this); // for reselect after reload
			confirmDelete(this, () -> {
				CalendarLocation calendarLocation = calendarSource.calendarLocation();
				calendarLocation.removeCalendarSource(calendarSource);
				R.calendarLocation().save(calendarLocation);
			});
		}

		private void edit() {
			treeGrid.select(this); // for reselect after reload
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
			showEditForm(calendarSource.calendarLocation().name() + " " + title, calendarSourceForm, () -> {
				calendarSourceForm.writeTo(calendarSource);
				R.calendarSource().save(calendarSource);
				return null;
			});
		}
	}

	final class TreeNodeCalendarEvent implements TreeNode {

		private final TreeNodeCalendarSource treeNodeCalendarSource;
		private final CalendarEvent calendarEvent;
		private final List<String> path;

		public TreeNodeCalendarEvent(TreeNodeCalendarSource treeNodeCalendarSource, CalendarEvent calendarEvent) {
			this.treeNodeCalendarSource = treeNodeCalendarSource;
			this.calendarEvent = calendarEvent;
			this.path = Lists.newArrayList(Iterables.concat(treeNodeCalendarSource.path(), List.of("CE" + calendarEvent.id())));
		}

		@Override
		public List<String> path() {
			return path;
		}
		@Override
		public boolean equals(Object o) {
			return o != null && Objects.equals(path(), ((TreeNode)o).path());
		}

		@Override
		public String text() {
			return calendarEvent.startDateTime().toString();
		}

		@Override
		public Icon enabled() {
			return null;
		}


		@Override
		public Component url() {
			return null;
		}

		@Override
		public String startDate() {
			return calendarEvent.startDateTime().toString();
		}

		@Override
		public String endDate() {
			return calendarEvent.endDateTime().toString();
		}

		@Override
		public CalendarSource calendarSource() {
			return treeNodeCalendarSource.calendarSource();
		}
		@Override
		public TreeNode parent() {
			return treeNodeCalendarSource;
		}

		@Override
		public Component status() {
			return null;
		}

		@Override
		public LocalDateTime updated() {
			return treeNodeCalendarSource.calendarSource.lastRun();
		}

		@Override
		public int childrenCount() {
			return 0;
		}

		@Override
		public String hint() {
			return "";
		}

		@Override
		public Collection<TreeNode> getChildren() {
			return List.of();
		}

		@Override
		public Component crudButtons() {
			return null;
		}
	}

	private static <T> List<TreeNode> treeNodes(Collection<T> businessObjects, Function<T, TreeNode> converter) {
		List<TreeNode> treeNodes = new ArrayList<>();
		businessObjects.stream().map(bo -> converter.apply(bo)).forEach(tn -> treeNodes.add(tn));
		return treeNodes;
	}

	private void showInsertForm(CalendarLocation calendarLocation, CalendarSource calendarSource, CalendarSourceForm calendarSourceForm,  CalendarSource calendarSourceDefault) {
		calendarSourceForm.populateWith(calendarSourceDefault != null ? calendarSourceDefault : calendarSource);

		new OkCancelDialog("Event", calendarSourceForm)
				.okLabel("Save")
				.onOk(() -> {
					try {
						calendarSourceForm.writeTo(calendarSource);
						calendarLocation.addCalendarSource(calendarSource);
						R.calendarLocation().save(calendarLocation);
						reloadTreeGrid();
					} catch (ValidationException e) {
						throw new RuntimeException(e);
					}
				})
				.open();
	}

	private void showEditForm(String title, Component form, Callable<Void> runnable) {
		new OkCancelDialog(title, form)
				.okLabel("Save")
				.onOk(() -> {
					try {
						runnable.call();
						reloadTreeGrid();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
				.open();
	}

	private void confirmDelete(TreeNode treeNode, Runnable runnable) {
		new OkCancelDialog("Remove " + treeNode.text(), new NativeLabel("Are you sure?"))
				.okLabel("Yes")
				.onOk(() -> {
					runnable.run();
					reloadTreeGrid();
				})
				.open();
	}

	private Anchor createUrlAnchor(String url) {
		if (url == null) {
			return null;
		}

		Span spanIcon = new Span();
		spanIcon.addClassName("fas");
		spanIcon.addClassName("fa-arrow-up-right-from-square");

		Span spanWrapper = new Span(spanIcon);
		spanWrapper.addClassName("icon");

		Anchor anchor = new Anchor(url, spanWrapper);
		anchor.setTarget("_blank");

		return anchor;
	}
}