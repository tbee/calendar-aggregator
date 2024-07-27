package nl.softworks.calendarAggregator.application.vdn.view;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import nl.softworks.calendarAggregator.application.vdn.form.LabelForm;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("/label")
@StyleSheet("context://../vaadin.css")
@RolesAllowed("ROLE_ADMIN")
public class LabelView extends AbstractCrudView<Label> {
	private static final Logger LOG = LoggerFactory.getLogger(LabelView.class);

	public LabelView() {
		super("Label"
				, Label::new
				, p -> R.label().save(p)
				, p -> R.label().delete(p)
				, () -> R.label().findAllByOrderByNameAsc()
				, LabelForm::new
				, grid -> {
					grid.addColumn(Label::name).setHeader("Name").setSortable(true);
					grid.addColumn(new ComponentRenderer<>(label -> {
						NativeLabel nativeLabel = new NativeLabel();
						nativeLabel.setText(label == null || label.labelGroup() == null ? "-" : label.labelGroup().name());
						return nativeLabel;
					})).setHeader("Group");
					grid.addColumn(Label::seqnr).setHeader("Seqnr").setSortable(true);
				});
		tabs.setSelectedTab(labelTab);
	}
}