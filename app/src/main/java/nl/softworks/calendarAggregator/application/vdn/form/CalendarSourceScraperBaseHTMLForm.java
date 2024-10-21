package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.theme.lumo.LumoIcon;
import nl.softworks.calendarAggregator.application.vdn.component.CrudIconButtonbar;
import nl.softworks.calendarAggregator.application.vdn.component.IconButton;
import nl.softworks.calendarAggregator.application.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourcePreprocess;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceScraperBaseHTML;
import nl.softworks.calendarAggregator.domain.entity.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CalendarSourceScraperBaseHTMLForm extends CalendarSourceScraperBaseForm {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarSourceScraperBaseHTMLForm.class);

	private final Binder<CalendarSourceScraperBaseHTML> binder = new Binder<>();

	private final TextField scrapeBlockStartTextField = new TextField("Scrape block start");
	private final TextField scrapeBlockEndTextField = new TextField("Scrape block end");
	private final TextField removeCharsTextField = new TextField("Remove chars");

	private final Grid<CalendarSourcePreprocess> preprocessGrid = new Grid<>(CalendarSourcePreprocess.class, false);
	private List<CalendarSourcePreprocess> preprocessGridItems;
	private ListDataProvider<CalendarSourcePreprocess> preprocessListDataProvider;

	public CalendarSourceScraperBaseHTMLForm() {
		scrapeFormLayout.add(scrapeBlockStartTextField, scrapeBlockEndTextField, removeCharsTextField);

		CrudIconButtonbar preprocessCrudIconButtonbar = new CrudIconButtonbar()
				.onInsert(() -> {
					preprocessGridItems.add(new CalendarSourcePreprocess());
					preprocessListDataProvider = new ListDataProvider<>(preprocessGridItems);
					preprocessGrid.setItems(preprocessListDataProvider);
				}) //this::insert)
				.onEdit(() -> {}) //this::edit)
				.onDelete(() -> {}) //() -> delete())
		;
		FormLayout preprocessFormLayout = addAsFormlayoutInAccordion("Preprocess", true, preprocessCrudIconButtonbar, preprocessGrid);
		preprocessFormLayout.setColspan(preprocessGrid, 2);

		// Setup preprocessGrid
		Grid.Column<CalendarSourcePreprocess> oldValueColumn = preprocessGrid.addColumn(CalendarSourcePreprocess::oldValue).setHeader("Regexp");
		Grid.Column<CalendarSourcePreprocess> newValueColumn = preprocessGrid.addColumn(CalendarSourcePreprocess::newValue).setHeader("Replacement");

		binder.forField(scrapeBlockStartTextField).bind(CalendarSourceScraperBaseHTML::scrapeBlockStart, CalendarSourceScraperBaseHTML::scrapeBlockStart);
		binder.forField(scrapeBlockEndTextField).bind(CalendarSourceScraperBaseHTML::scrapeBlockEnd, CalendarSourceScraperBaseHTML::scrapeBlockEnd);
		binder.forField(removeCharsTextField).bind(CalendarSourceScraperBaseHTML::removeChars, CalendarSourceScraperBaseHTML::removeChars);

		
		// Also allow inline editing. See what is more pleasant (because it is a different UX).
		// See https://vaadin.com/forum/t/consume-key-event/166801/6
		Editor<CalendarSourcePreprocess> preprocessGridEditor = preprocessGrid.getEditor();
		Binder<CalendarSourcePreprocess> preprocessGridBinder = new Binder<>(CalendarSourcePreprocess.class);
		preprocessGridEditor.setBinder(preprocessGridBinder);
		preprocessGridEditor.setBuffered(true);

		TextField oldValueTextField = new TextField();
		oldValueTextField.setWidthFull();
		oldValueColumn.setEditorComponent(oldValueTextField);
		preprocessGridBinder.forField(oldValueTextField).bind(CalendarSourcePreprocess::oldValue, CalendarSourcePreprocess::oldValue);

		TextField newValueTextField = new TextField();
		newValueTextField.setWidthFull();
		newValueColumn.setEditorComponent(newValueTextField);
		preprocessGridBinder.forField(newValueTextField).bind(CalendarSourcePreprocess::newValue, CalendarSourcePreprocess::newValue);

		oldValueTextField.getElement().addEventListener("keydown", e -> {
			preprocessGridEditor.cancel();
		}).setFilter("event.code === 'Escape'").addEventData("event.stopPropagation()");
		oldValueTextField.addBlurListener(e -> {
			if (preprocessGridEditor.isOpen()) {
				preprocessGridEditor.save();
			}
		});
		newValueTextField.getElement().addEventListener("keydown", e -> {
			preprocessGridEditor.cancel();
		}).setFilter("event.code === 'Escape'").addEventData("event.stopPropagation()");
		newValueTextField.addBlurListener(e -> {
			if (preprocessGridEditor.isOpen()) {
				preprocessGridEditor.save();
			}
		});

		preprocessGrid.addItemDoubleClickListener(e -> {
			if (!preprocessGridEditor.isOpen()) {
				preprocessGridEditor.editItem(e.getItem());
				Component editorComponent = e.getColumn().getEditorComponent();
				if (editorComponent instanceof Focusable) {
					((Focusable) editorComponent).focus();
				}
			}
		});
	}

	@Override
	public CalendarSourceScraperBaseHTMLForm populateWith(CalendarSource calendarSource) {
		super.populateWith(calendarSource);
		if (calendarSource instanceof CalendarSourceScraperBaseHTML calendarSourceScraperBaseHTML) {
			binder.readBean(calendarSourceScraperBaseHTML);

			preprocessGridItems = new ArrayList<>(calendarSourceScraperBaseHTML.calendarSourcePreprocesses());
			preprocessListDataProvider = new ListDataProvider<>(preprocessGridItems);
			preprocessGrid.setItems(preprocessListDataProvider);
		}
		return this;
	}

	@Override
	public CalendarSourceScraperBaseHTMLForm writeTo(CalendarSource calendarSource) throws ValidationException {
		super.writeTo(calendarSource);
		if (calendarSource instanceof CalendarSourceScraperBaseHTML calendarSourceScraperBaseHTML) {
			binder.writeBean(calendarSourceScraperBaseHTML);
			calendarSourceScraperBaseHTML.calendarSourcePreprocesses(preprocessGridItems);
		}
		return this;
	}

}