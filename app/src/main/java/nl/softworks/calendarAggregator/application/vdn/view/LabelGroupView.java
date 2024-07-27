package nl.softworks.calendarAggregator.application.vdn.view;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import nl.softworks.calendarAggregator.application.vdn.form.LabelGroupForm;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.LabelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("/labelgroup")
@StyleSheet("context://../vaadin.css")
@RolesAllowed("ROLE_ADMIN")
public class LabelGroupView extends AbstractCrudView<LabelGroup> {
	private static final Logger LOGGER = LoggerFactory.getLogger(LabelGroupView.class);

	public LabelGroupView() {
		super("Label group"
				, LabelGroup::new
				, p -> R.labelGroup().save(p)
				, p -> R.labelGroup().delete(p)
				, () -> R.labelGroup().findAllByOrderByNameAsc()
				, LabelGroupForm::new
				, grid -> {
					grid.addColumn(LabelGroup::name).setHeader("Name");
				});
		tabs.setSelectedTab(labelGroupTab);
	}
}