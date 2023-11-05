package nl.softworks.calendarAggregator.boundary.vdn;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route("/manual")
@StyleSheet("context://../vaadin.css")
//@RolesAllowed("ROLE_PLANNER")
@PermitAll
public class ManualView extends CalendarAggregatorAppLayout
implements AfterNavigationObserver {
	private static final Logger LOG = LoggerFactory.getLogger(ManualView.class);
	private static final DateTimeFormatter YYYYMMDDHHMM = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final ListBox<CalendarSource> calendarSourceListBox;
	private final ListBox<CalendarEvent> calendarEventListBox;

	public ManualView() {
		super("Manual");
		tabs.setSelectedTab(manualTab);

		calendarSourceListBox = new ListBox<>();
//		calendarSourceListBox.getStyle().set( "border" , "1px solid black" ) ;
		calendarSourceListBox.setRenderer(new ComponentRenderer<>(calendarSource -> {
			Span nameSpan = new Span(calendarSource.getName());
			return nameSpan;
		}));
		calendarSourceListBox.addValueChangeListener(new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ListBox<CalendarSource>, CalendarSource>>() {
			@Override
			public void valueChanged(AbstractField.ComponentValueChangeEvent<ListBox<CalendarSource>, CalendarSource> event) {
				CalendarSource calendarSource = event.getValue();
				calendarEventListBox.setItems(calendarSource == null ? List.of() : calendarSource.getCalendarEvents());
			}
		});

		calendarEventListBox = new ListBox<>();
//		calendarEventListBox.getStyle().set( "border" , "1px solid black" ) ;
		calendarEventListBox.setRenderer(new ComponentRenderer<>(calendarEvent -> {
			Span startSpan = new Span(calendarEvent.getStartDateTime().format(YYYYMMDDHHMM));
			Span endSpan = new Span(calendarEvent.getEndDateTime().format(YYYYMMDDHHMM));

			HorizontalLayout row = new HorizontalLayout();
			row.setAlignItems(FlexComponent.Alignment.CENTER);
			row.add(startSpan, endSpan);
			row.getStyle().set("line-height", "var(--lumo-line-height-m)");
			return row;
		}));


		HorizontalLayout content = new HorizontalLayout();
//		content.setAlignItems(FlexComponent.Alignment.START);
		content.add(vertical(new NativeLabel("Source"), calendarSourceListBox), vertical(new NativeLabel("Events"), calendarEventListBox));
		setContent(content);
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		List<CalendarSource> calendarSources = R.calendarSource().findAll();
		calendarSourceListBox.setItems(calendarSources);
	}

	private VerticalLayout vertical(Component... children) {
		VerticalLayout verticalLayout = new VerticalLayout(children);
		return verticalLayout;
	}
	private VerticalLayout box(Component... children) {
		VerticalLayout verticalLayout = new VerticalLayout(children);
//		verticalLayout.addClassName("border1px");
		verticalLayout.getStyle().set( "border" , "1px solid black" ) ;
		return verticalLayout;
	}
}