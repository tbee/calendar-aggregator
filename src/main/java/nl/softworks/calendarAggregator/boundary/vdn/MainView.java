package nl.softworks.calendarAggregator.boundary.vdn;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

	private TreeGrid<TreeNode> treeGrid;

	public MainView() {
		super("Overview");
		tabs.setSelectedTab(overviewTab);
		treeGrid = new TreeGrid<TreeNode>();
		treeGrid.addHierarchyColumn(TreeNode::getText).setHeader("Name");
		treeGrid.addColumn(TreeNode::getStartDate).setHeader("Start");
		treeGrid.addColumn(TreeNode::getEndDate).setHeader("End");
		setContent(treeGrid);
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		List<CalendarSource> calendarSources = R.calendarSource().findAll();
		List<TreeNode> treeNodes = treeNodes(calendarSources, TreeNodeCalendarSource::new);
		treeGrid.setItems(treeNodes, this::getTreeNodeChildren);
	}

	sealed interface TreeNode permits TreeNodeCalendarSource, TreeNodeCalendarEvent {
		String getText();
		String getStartDate();
		String getEndDate();
	}

	record TreeNodeCalendarSource(CalendarSource calendarSource) implements TreeNode {
		@Override
		public String getText() {
			return calendarSource().getName();
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
	record TreeNodeCalendarEvent (CalendarEvent calendarEvent) implements TreeNode {
		@Override
		public String getText() {
			return calendarEvent.getSubject();
		}

		@Override
		public String getStartDate() {
			return calendarEvent.getStartDateTime().format(YYYYMMDDHHMM);
		}

		@Override
		public String getEndDate() {
			return calendarEvent.getEndDateTime().format(YYYYMMDDHHMM);
		}
	}

	public List<TreeNode> getTreeNodeChildren(TreeNode treeNode) {
		if (treeNode instanceof TreeNodeCalendarSource treeNodeCalendarSource) {
			return treeNodes(treeNodeCalendarSource.calendarSource().getCalendarEvents(), TreeNodeCalendarEvent::new);
		}
		return List.of();
	}

	private <T> List<TreeNode> treeNodes(Collection<T> businessObjects, Function<T, TreeNode> converter) {
		List<TreeNode> treeNodes = new ArrayList<>();
		businessObjects.stream().map(bo -> converter.apply(bo)).forEach(tn -> treeNodes.add(tn));
		return treeNodes;
	}
}