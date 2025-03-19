package nl.softworks.calendarAggregator.application.vdn.view;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import nl.softworks.calendarAggregator.application.vdn.CalendarAggregatorAppLayout;
import nl.softworks.calendarAggregator.application.vdn.form.LabelGroupForm;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.LabelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.webstack.vdn.component.CrudComponent;

@Route("/labelgroup")
@StyleSheet("context://../vaadin.css")
@RolesAllowed("ROLE_ADMIN")
public class LabelGroupView extends CalendarAggregatorAppLayout implements AfterNavigationObserver {
	private static final Logger LOGGER = LoggerFactory.getLogger(LabelGroupView.class);

	private final CrudComponent<LabelGroup> crudComponent;

	public LabelGroupView() {
		super("Label group");
		tabs.setSelectedTab(labelGroupTab);
		setContent(crudComponent = new CrudComponent<>(getPageTitle()
				, LabelGroup::new
				, p -> R.labelGroup().save(p)
				, p -> R.labelGroup().delete(p)
				, () -> R.labelGroup().findAllByOrderByNameAsc()
				, LabelGroupForm::new
				, grid -> {
					grid.addColumn(LabelGroup::name).setHeader("Name");
				}));
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		crudComponent.reloadGrid();
	}
}