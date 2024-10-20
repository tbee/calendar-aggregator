package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.theme.lumo.LumoIcon;
import nl.softworks.calendarAggregator.application.vdn.component.IconButton;
import nl.softworks.calendarAggregator.application.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.application.vdn.component.ResultDialog;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceLabelAssignment;
import nl.softworks.calendarAggregator.domain.entity.Label;
import nl.softworks.calendarAggregator.domain.entity.Timezone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

abstract public class CalendarSourceForm extends VerticalLayout {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarSourceForm.class);

	private final Binder<CalendarSource> binder = new Binder<>();

	private final TextField descriptionTextfield = new TextField("Description");
	private final TextField statusTextField = new TextField("Status");
	private final Checkbox enabledCheckbox = new Checkbox("Enabled");
	private final TextField urlTextField = new TextField("URL");
	private final Grid<LabelAssignmentGridRow> labelAssignGrid = new Grid<>(LabelAssignmentGridRow.class, false);
	private final List<LabelAssignmentGridRow> labelAssignGridItems;
	private final ListDataProvider<LabelAssignmentGridRow> labelAssignListDataProvider;
	private final Checkbox hiddenCheckbox = new Checkbox("Hidden");
	private final ComboBox<Timezone> timezoneComboBox = new ComboBox<>("Timezone");

	private CalendarSource calendarSource;

	public CalendarSourceForm() {
		setWidthFull();

		urlTextField.setTooltipText("If more-info URL differs from the one in location");
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
		formLayout.setColspan(labelAssignGrid, 2);
		formLayout.setColspan(urlTextField, 2);
		formLayout.setColspan(generateButton, 2);

		addAsFormlayoutInAccordion("Labels", true, labelAssignGrid);

		// Setup labelAssignGrid
		labelAssignGrid.addComponentColumn(LabelAssignmentGridRow::selected).setHeader("").setWidth("60px").setFlexGrow(0);
		labelAssignGrid.addColumn(LabelAssignmentGridRow::name).setHeader("Label");
		labelAssignGrid.addComponentColumn(LabelAssignmentGridRow::editButton).setHeader("").setWidth("60px").setFlexGrow(0);
		Grid.Column<LabelAssignmentGridRow> subjectRegexpColumn = labelAssignGrid.addColumn(LabelAssignmentGridRow::subjectRegexp).setHeader("Subject regexp");

		binder.forField(descriptionTextfield).bind(CalendarSource::description, CalendarSource::description);
		binder.forField(statusTextField).bind(CalendarSource::status, CalendarSource::status);
		binder.forField(enabledCheckbox).bind(CalendarSource::enabled, CalendarSource::enabled);
		binder.forField(urlTextField).bind(CalendarSource::url, CalendarSource::url);
		binder.forField(hiddenCheckbox).bind(CalendarSource::hidden, CalendarSource::hidden);
		binder.forField(timezoneComboBox).bind(CalendarSource::timezone, CalendarSource::timezone);
		labelAssignGridItems = R.label().findAllByOrderBySeqnrAsc().stream().map(LabelAssignmentGridRow::new).toList();
		labelAssignListDataProvider = new ListDataProvider<>(labelAssignGridItems);
		labelAssignGrid.setItems(labelAssignListDataProvider);


		// Also allow inline editing. See what is more pleasant (because it is a different UX).
		// See https://vaadin.com/forum/t/consume-key-event/166801/6
		Editor<LabelAssignmentGridRow> labelAssignGridEditor = labelAssignGrid.getEditor();
		Binder<LabelAssignmentGridRow> labelAssignGridBinder = new Binder<>(LabelAssignmentGridRow.class);
		labelAssignGridEditor.setBinder(labelAssignGridBinder);
		labelAssignGridEditor.setBuffered(true);

		TextField subjectRegexpTextField = new TextField();
		subjectRegexpTextField.setWidthFull();
		subjectRegexpColumn.setEditorComponent(subjectRegexpTextField);
		labelAssignGridBinder.forField(subjectRegexpTextField).bind(LabelAssignmentGridRow::subjectRegexp, LabelAssignmentGridRow::subjectRegexp);
		subjectRegexpTextField.getElement().addEventListener("keydown", e -> {
			labelAssignGridEditor.cancel();
		}).setFilter("event.code === 'Escape'").addEventData("event.stopPropagation()");
		subjectRegexpTextField.addBlurListener(e -> {
			if (labelAssignGridEditor.isOpen()) {
				labelAssignGridEditor.save();
			}
		});

		labelAssignGrid.addItemDoubleClickListener(e -> {
			labelAssignGridEditor.editItem(e.getItem());
			Component editorComponent = e.getColumn().getEditorComponent();
			if (editorComponent instanceof Focusable) {
				((Focusable) editorComponent).focus();
			}
		});
	}

	public CalendarSourceForm populateWith(CalendarSource calendarSource) {
		timezoneComboBox.setItems(R.timezone().findAll());
		binder.readBean(calendarSource);
		this.calendarSource = calendarSource;

		Map<Label, CalendarSourceLabelAssignment> assignedLabels = calendarSource.labelAssignments().stream().collect(Collectors.toMap(CalendarSourceLabelAssignment::label, la -> la));
		labelAssignGridItems.forEach(la -> {
			la.clear();
			if (assignedLabels.containsKey(la.label)) {
				la.populateWith(assignedLabels.get(la.label));
			}
		});
		return this;
	}

	public CalendarSourceForm writeTo(CalendarSource calendarSource) throws ValidationException {
		binder.writeBean(calendarSource);
		List<CalendarSourceLabelAssignment> selectedLabelAssignments = labelAssignGridItems.stream()
				.filter(la -> la.selectedCheckbox.getValue())
				.map(la -> la.assign)
				.toList();
		calendarSource.labelAssignments(selectedLabelAssignments);
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
							labelAssignListDataProvider.refreshItem(LabelAssignmentGridRow.this);
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

	protected FormLayout addAsFormlayoutInAccordion(String title, boolean folded, Component... components) {
		Accordion accordion = new Accordion();
		FormLayout layout = new FormLayout(components);
		accordion.add(title, layout);
		add(accordion);
		accordion.setWidthFull();

		if (folded) {
			accordion.close();
		}
		return layout;
	}
}