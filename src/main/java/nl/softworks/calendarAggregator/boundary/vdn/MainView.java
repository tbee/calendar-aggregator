package nl.softworks.calendarAggregator.boundary.vdn;

import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("/")
//@StyleSheet("context://../vaadin.css")
//@RolesAllowed("ROLE_PLANNER")
@PermitAll
public class MainView extends CalendarAggregatorAppLayout
{
	private static final Logger LOG = LoggerFactory.getLogger(MainView.class);

	public MainView() {
		super("Calendar aggregator");
	}
}