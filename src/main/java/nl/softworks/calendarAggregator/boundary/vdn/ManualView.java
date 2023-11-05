package nl.softworks.calendarAggregator.boundary.vdn;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
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

import java.time.LocalDateTime;
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
	private final TextField nameTextField = new TextField("Name");
	private final TextField urlTextField = new TextField("Url");
	private final DatePicker startDatePicker = new DatePicker("Start date");
	private final TimePicker startTimePicker = new TimePicker("Start time");
	private final DatePicker endDatePicker = new DatePicker("End date");
	private final TimePicker endTimePicker = new TimePicker("End time");
	private final TextField summaryTextField = new TextField("Summary");

	public ManualView() {
		super("Manual");
		tabs.setSelectedTab(manualTab);

		calendarSourceListBox = new ListBox<>();
//		calendarSourceListBox.getStyle().set( "border" , "1px solid black" ) ;
		calendarSourceListBox.setRenderer(new ComponentRenderer<>(calendarSource -> {
			Span nameSpan = new Span(calendarSource.getName());
			return nameSpan;
		}));
		calendarSourceListBox.addValueChangeListener(e -> setFormFields(e.getValue()));

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
		calendarEventListBox.addValueChangeListener(e -> setFormFields(e.getValue()));

		FormLayout sourceFormLayout = new FormLayout();
		sourceFormLayout.add(nameTextField, urlTextField);

		FormLayout eventFormLayout = new FormLayout();
		eventFormLayout.add(startDatePicker, startTimePicker, endDatePicker, endTimePicker, summaryTextField);
		eventFormLayout.setColspan(summaryTextField, 2);

		HorizontalLayout content = new HorizontalLayout();
		content.setAlignItems(FlexComponent.Alignment.START);
		content.add(vertical(title("Sources"), calendarSourceListBox)
				, new Divider()
				, vertical(title("Source"), sourceFormLayout, new NativeLabel("Events"), calendarEventListBox)
				, new Divider()
				, vertical(title("Event"), eventFormLayout));
		setContent(content);
	}

	private static NativeLabel title(String text) {
		NativeLabel nativeLabel = new NativeLabel(text);
		nativeLabel.addClassName("title");
		return nativeLabel;
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		List<CalendarSource> calendarSources = R.calendarSource().findAll();
		calendarSourceListBox.setItems(calendarSources);
	}

	private void setFormFields(CalendarSource calendarSource) {
		nameTextField.setValue(calendarSource == null ? null : calendarSource.getName());
		urlTextField.setValue(calendarSource == null ? null : calendarSource.getUrl());
		calendarEventListBox.setItems(calendarSource == null ? List.of() : calendarSource.getCalendarEvents());
	}

	private void setFormFields(CalendarEvent calendarEvent) {
		LocalDateTime startDateTime = calendarEvent.getStartDateTime();
		LocalDateTime endDateTime = calendarEvent.getEndDateTime();
		String subject = calendarEvent.getSubject();

		startDatePicker.setValue(startDateTime == null ? null : startDateTime.toLocalDate());
		startTimePicker.setValue(startDateTime == null ? null : startDateTime.toLocalTime());
		endDatePicker.setValue(endDateTime == null ? null : endDateTime.toLocalDate());
		endTimePicker.setValue(endDateTime == null ? null : calendarEvent.getEndDateTime().toLocalTime());
		summaryTextField.setValue(subject);
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

	public class Divider extends Span {

		public Divider() {
			getStyle().set("background-color", "silver");
			getStyle().set("flex", "0 0 2px");
			getStyle().set("align-self", "stretch");
			getStyle().set("margin", "5px");
		}
	}
}