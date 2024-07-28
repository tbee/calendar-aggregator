package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.BlurNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyDownEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import nl.softworks.calendarAggregator.application.vdn.component.ResultDialog;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceLabelAssignment;
import nl.softworks.calendarAggregator.domain.entity.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

abstract public class CalendarSourceForm extends FormLayout {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarSourceForm.class);

	private final Binder<CalendarSource> binder = new Binder<>();

	private final TextField descriptionTextfield = new TextField("Description");
	private final TextField statusTextField = new TextField("Status");
	private final Checkbox enabledCheckbox = new Checkbox("Enabled");
	private final TextField urlTextField = new TextField("URL");
	private final Grid<LabelAssignmentGridRow> labelAssignGrid = new Grid<>(LabelAssignmentGridRow.class, false);
	private final List<LabelAssignmentGridRow> labelAssignGridItems;

	private CalendarSource calendarSource;

	public CalendarSourceForm() {
		setColspan(statusTextField, 2);
		setColspan(labelAssignGrid, 2);
		setColspan(urlTextField, 2);
		urlTextField.setTooltipText("If more-info URL differs from the one in location");
		add(descriptionTextfield, enabledCheckbox, urlTextField, labelAssignGrid, statusTextField);

		Button generateButton = new Button("Generate", evt -> generate());
		setColspan(generateButton, 2);
		add(generateButton);

		binder.forField(descriptionTextfield).bind(CalendarSource::description, CalendarSource::description);
		binder.forField(statusTextField).bind(CalendarSource::status, CalendarSource::status);
		binder.forField(enabledCheckbox).bind(CalendarSource::enabled, CalendarSource::enabled);
		binder.forField(urlTextField).bind(CalendarSource::url, CalendarSource::url);

		// Setup labelAssignGrid
		labelAssignGrid.addComponentColumn(LabelAssignmentGridRow::selected).setHeader("").setWidth("60px").setFlexGrow(0);
		labelAssignGrid.addColumn(LabelAssignmentGridRow::name).setHeader("Label");
		Grid.Column<LabelAssignmentGridRow> subjectRegexpColumn = labelAssignGrid.addColumn(LabelAssignmentGridRow::subjectRegexp).setHeader("Subject regexp");
		labelAssignGridItems = R.label().findAllByOrderByNameAsc().stream().map(LabelAssignmentGridRow::new).toList();
		labelAssignGrid.setItems(new ListDataProvider<>(labelAssignGridItems));

		// Setup inline editing labelAssignGrid
		Editor<LabelAssignmentGridRow> editor = labelAssignGrid.getEditor();
		TextField subjectRegexpTextField = new TextField();
		subjectRegexpTextField.addBlurListener((ComponentEventListener<BlurNotifier.BlurEvent<TextField>>) event -> {
			editor.getItem().subjectRegexp(subjectRegexpTextField.getValue());
			editor.closeEditor();
		});
		subjectRegexpTextField.addKeyDownListener(Key.ESCAPE, (ComponentEventListener<KeyDownEvent>) event -> {
			editor.cancel();
		});
		subjectRegexpTextField.setWidthFull();
		subjectRegexpColumn.setEditorComponent(subjectRegexpTextField);

		// On double click open inline editing
		labelAssignGrid.addItemDoubleClickListener(e -> {
			editor.editItem(e.getItem());
			subjectRegexpTextField.setValue(e.getItem().subjectRegexp());
			Component editorComponent = e.getColumn().getEditorComponent();
			if (editorComponent instanceof Focusable focusable) {
				focusable.focus();
			}
		});
	}

	public CalendarSourceForm populateWith(CalendarSource calendarSource) {
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
			assign = (assign != null ? assign : new CalendarSourceLabelAssignment(calendarSource, label));
			assign.subjectRegexp(v);
		}

		public Component selected() {
			return selectedCheckbox;
		}

		public void edit() {
		}
	}
}