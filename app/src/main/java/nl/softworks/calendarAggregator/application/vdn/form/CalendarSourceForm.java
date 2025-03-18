package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.theme.lumo.LumoIcon;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceLabelAssignment;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourcePreprocess;
import nl.softworks.calendarAggregator.domain.entity.Label;
import nl.softworks.calendarAggregator.domain.entity.Timezone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.webstack.vdn.component.AnchorIcon;
import org.tbee.webstack.vdn.component.CrudIconButtonbar;
import org.tbee.webstack.vdn.component.EditingGrid;
import org.tbee.webstack.vdn.component.Harmonica;
import org.tbee.webstack.vdn.component.IconButton;
import org.tbee.webstack.vdn.component.OkCancelDialog;
import org.tbee.webstack.vdn.component.ResultDialog;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

abstract public class CalendarSourceForm extends Harmonica {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarSourceForm.class);

	private final Binder<CalendarSource> binder = new Binder<>();

	private final TextField descriptionTextfield = new TextField("Description");
	private final TextField statusTextField = new TextField("Status");
	private final Checkbox enabledCheckbox = new Checkbox("Enabled");
	private final TextField urlTextField = new TextField("URL");
	private final EditingGrid<LabelAssignmentGridRow> labelAssignGrid = new EditingGrid<>(LabelAssignmentGridRow.class, false);
	private final Checkbox hiddenCheckbox = new Checkbox("Hidden");
	private final ComboBox<Timezone> timezoneComboBox = new ComboBox<>("Timezone");
	private final EditingGrid<CalendarSourcePreprocess> preprocessGrid = new EditingGrid<>(CalendarSourcePreprocess.class, false);

	private CalendarSource calendarSource;

	public CalendarSourceForm() {
		setWidthFull();

		urlTextField.setTooltipText("If more-info URL differs from the one in location. Use `=@{now.plusYears(1).format(yyyy_MM_dd)}` to insert code.");
		timezoneComboBox.setItemLabelGenerator(timezone -> timezone == null ? "-" : timezone.name());
		timezoneComboBox.setRenderer(new ComponentRenderer<>(timezone -> {
			Span nameSpan = new Span(timezone == null ? "-" : timezone.name());
			return nameSpan;
		}));
		timezoneComboBox.setClearButtonVisible(true);
		timezoneComboBox.setTooltipText("This is the timezone in which the data is provided, this may deviate from the timezone the location is in.");

		Button generateButton = new Button("Generate", evt -> generate());

		FormLayout formLayout = addAsFormlayoutInAccordion("Source", true, descriptionTextfield, enabledCheckbox, timezoneComboBox, hiddenCheckbox, urlTextField, statusTextField, generateButton);
		formLayout.setColspan(statusTextField, 2);
		formLayout.setColspan(urlTextField, 2);
		formLayout.setColspan(generateButton, 2);

		FormLayout formLayoutLabelAssign = addAsFormlayoutInAccordion("Labels", true, labelAssignGrid);
		formLayoutLabelAssign.setColspan(labelAssignGrid, 2);

		// Setup labelAssignGrid
		labelAssignGrid.addComponentColumn(LabelAssignmentGridRow::selected).setHeader("").setWidth("60px").setFlexGrow(0);
		labelAssignGrid.addColumn(LabelAssignmentGridRow::name).setHeader("Label");
		labelAssignGrid.addComponentColumn(LabelAssignmentGridRow::editButton).setHeader("").setWidth("60px").setFlexGrow(0);
		labelAssignGrid.addStringColumn(LabelAssignmentGridRow::subjectRegexp, LabelAssignmentGridRow::subjectRegexp).setHeader("Subject regexp");

		binder.forField(descriptionTextfield).bind(CalendarSource::description, CalendarSource::description);
		binder.forField(statusTextField).bind(CalendarSource::status, CalendarSource::status);
		binder.forField(enabledCheckbox).bind(CalendarSource::enabled, CalendarSource::enabled);
		binder.forField(urlTextField).bind(CalendarSource::url, CalendarSource::url);
		binder.forField(hiddenCheckbox).bind(CalendarSource::hidden, CalendarSource::hidden);
		binder.forField(timezoneComboBox).bind(CalendarSource::timezone, CalendarSource::timezone);
		labelAssignGrid.setItems(R.label().findAllByOrderBySeqnrAsc().stream().map(LabelAssignmentGridRow::new).toList());

		CrudIconButtonbar preprocessCrudIconButtonbar = new CrudIconButtonbar()
				.onInsert(() -> preprocessGrid.addItems(new CalendarSourcePreprocess()));
		FormLayout preprocessFormLayout = addAsFormlayoutInAccordion("Preprocess", true, preprocessCrudIconButtonbar, preprocessGrid);
		preprocessFormLayout.setColspan(preprocessGrid, 2);

		// Setup preprocessGrid
		preprocessGrid.addStringColumn(CalendarSourcePreprocess::oldValue, CalendarSourcePreprocess::oldValue).setHeader("Regexp");
		preprocessGrid.addStringColumn(CalendarSourcePreprocess::newValue, CalendarSourcePreprocess::newValue).setHeader("Replacement");
		preprocessGrid.addCrudIconButtonbarColumn();
		preprocessGrid.addComponentColumn((ValueProvider<CalendarSourcePreprocess, Component>) bean -> AnchorIcon.jumpOut("https://www.baeldung.com/string/replace-all")).setWidth("20px");

		preprocessGrid.onEdit(item -> {
			CalendarSourcePreprocessForm form = new CalendarSourcePreprocessForm().populateWith(item);
			new OkCancelDialog("Preprocess", form)
					.width(50, Unit.PERCENTAGE)
					.okLabel("Accept")
					.onOk(() -> {
						try {
							form.writeTo(item);
							preprocessGrid.refresh();
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					})
					.open();
		});
	}

	public CalendarSourceForm populateWith(CalendarSource calendarSource) {
		timezoneComboBox.setItems(R.timezone().findAll());
		binder.readBean(calendarSource);
		this.calendarSource = calendarSource;

		Map<Label, CalendarSourceLabelAssignment> assignedLabels = calendarSource.labelAssignments().stream().collect(Collectors.toMap(CalendarSourceLabelAssignment::label, la -> la));
		labelAssignGrid.getItems().forEach(la -> {
			la.clear();
			if (assignedLabels.containsKey(la.label)) {
				la.populateWith(assignedLabels.get(la.label));
			}
		});
		preprocessGrid.setItems(calendarSource.calendarSourcePreprocesses());
		return this;
	}

	public CalendarSourceForm writeTo(CalendarSource calendarSource) throws ValidationException {
		binder.writeBean(calendarSource);
		List<CalendarSourceLabelAssignment> selectedLabelAssignments = labelAssignGrid.getItems().stream()
				.filter(la -> la.selectedCheckbox.getValue())
				.map(la -> la.assign)
				.toList();
		calendarSource.labelAssignments(selectedLabelAssignments);
		calendarSource.calendarSourcePreprocesses(preprocessGrid.getItems());
		return this;
	}

	/*
	 * This uses the actual source, so it can be saved
	 */
	private void generate() {
		generateAndShowTrace(calendarSource);
	}

	protected void generateAndShowTrace(CalendarSource calendarSource) {
		try {
			writeTo(calendarSource);
			List<CalendarEvent> calendarEvents = calendarSource.generateEvents();
			populateWith(calendarSource);

			String calendarEventsString = calendarEvents.stream().map(s -> s + "\n").collect(Collectors.joining());
			calendarSource.logAppend("\n\n" + calendarEventsString);
		}
		catch (ValidationException | RuntimeException e) {
			StringWriter stringWriter = new StringWriter();
			e.printStackTrace(new PrintWriter(stringWriter));
			calendarSource.logAppend(stringWriter.toString());
		}

		new ResultDialog(calendarSource.log()).open();
	}

	class LabelAssignmentGridRow {

		final private Label label;
		private CalendarSourceLabelAssignment assign;
		private Checkbox selectedCheckbox = new Checkbox(false);
		private IconButton editIcon = new IconButton(LumoIcon.EDIT.create(), e -> edit());

		public LabelAssignmentGridRow(Label v) {
			this.label = v;
			selectedCheckbox.addValueChangeListener(event -> {
                if (selectedCheckbox.getValue() && assign == null) {
                    assign = new CalendarSourceLabelAssignment(calendarSource, label);
                }
            });
		}

		public void clear() {
			selectedCheckbox.setValue(false);
			assign = null;
		}

		public void populateWith(CalendarSourceLabelAssignment calendarSourceLabelAssignment) {
			assign = calendarSourceLabelAssignment;
			selectedCheckbox.setValue(true);
		}

		public String name() {
			return label.name();
		}

		public String subjectRegexp() {
			return assign == null ? "" : assign.subjectRegexp();
		}
		public void subjectRegexp(String v) {
			assign.subjectRegexp(v);
		}

		public Component selected() {
			return selectedCheckbox;
		}

		public IconButton editButton() {
			return editIcon;
		}

		public void edit() {
			assign = (assign != null ? assign : new CalendarSourceLabelAssignment(calendarSource, label));
			TextField textField = new TextField();
			textField.setWidthFull();
			textField.setValue(assign.subjectRegexp() == null ? "" : assign.subjectRegexp());
			new OkCancelDialog("Subject regexp", textField)
					.width(50, Unit.PERCENTAGE)
					.okLabel("Accept")
					.onOk(() -> {
						try {
							assign.subjectRegexp(textField.getValue());
							LabelAssignmentGridRow.this.selectedCheckbox.setValue(true);
							labelAssignGrid.refresh(LabelAssignmentGridRow.this);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					})
					.open();
		}
	}

	protected FormLayout addAsFormlayoutInAccordion(String title, Component... components) {
		return addAsFormlayoutInAccordion(title, false, components);
	}

	protected FormLayout addAsFormlayoutInAccordion(String title, boolean closed, Component... components) {
		FormLayout formLayout = new FormLayout(components);
		add(title, formLayout);
		if (closed) {
			close(title);
		}
		return formLayout;
	}
}