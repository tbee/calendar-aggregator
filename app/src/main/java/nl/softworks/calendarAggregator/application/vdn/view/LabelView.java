package nl.softworks.calendarAggregator.application.vdn.view;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import nl.softworks.calendarAggregator.application.vdn.CalendarAggregatorAppLayout;
import nl.softworks.calendarAggregator.application.vdn.form.LabelForm;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.webstack.vdn.component.CrudComponent;

import java.util.List;

@Route("/label")
@StyleSheet("context://../vaadin.css")
@RolesAllowed("ROLE_ADMIN")
public class LabelView extends CalendarAggregatorAppLayout implements AfterNavigationObserver {
	private static final Logger LOGGER = LoggerFactory.getLogger(LabelView.class);

	private final CrudComponent<Label> crudComponent;

	public LabelView() {
		super("Label");
		tabs.setSelectedTab(labelGroupTab);
		setContent(crudComponent = new CrudComponent<>(getPageTitle()
				, Label::new
				, p -> R.label().save(p)
				, p -> R.label().delete(p)
				, () -> R.label().findAllByOrderByNameAsc()
				, LabelForm::new
				, grid -> {
					Grid.Column<Label> seqnrColumn = grid.addColumn(Label::seqnr).setHeader("Seqnr").setSortable(true);
					grid.addColumn(Label::name).setHeader("Name").setSortable(true);
					grid.addColumn(new ComponentRenderer<>(label -> {
						NativeLabel nativeLabel = new NativeLabel();
						nativeLabel.setText(label == null || label.labelGroup() == null ? "-" : label.labelGroup().name());
						return nativeLabel;
					})).setHeader("Group");
					grid.addColumn(Label::icon).setHeader("Icon").setSortable(true);
					grid.addColumn(new ComponentRenderer<>(label -> {
						Span badge = new Span(label.icon());
						badge.getElement().getThemeList().add("badge primary");
						badge.getElement().getStyle().set("color", label.labelGroup().color());
						badge.getElement().getStyle().set("background", label.labelGroup().background());
						return badge;
					})).setHeader("Icon visual");
					grid.sort(List.of(new GridSortOrder<>(seqnrColumn, SortDirection.ASCENDING)));
				}));
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		crudComponent.reloadGrid();
	}
}