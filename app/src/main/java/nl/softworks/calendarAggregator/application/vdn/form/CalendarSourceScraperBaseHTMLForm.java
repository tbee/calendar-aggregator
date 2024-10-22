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
import nl.softworks.calendarAggregator.application.vdn.component.EditingGrid;
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
	private final EditingGrid<CalendarSourcePreprocess> preprocessGrid = new EditingGrid<>(CalendarSourcePreprocess.class, false);

	public CalendarSourceScraperBaseHTMLForm() {
		scrapeFormLayout.add(scrapeBlockStartTextField, scrapeBlockEndTextField, removeCharsTextField);

		CrudIconButtonbar preprocessCrudIconButtonbar = new CrudIconButtonbar()
				.onInsert(() -> preprocessGrid.addItems(new CalendarSourcePreprocess()));
		FormLayout preprocessFormLayout = addAsFormlayoutInAccordion("Preprocess", true, preprocessCrudIconButtonbar, preprocessGrid);
		preprocessFormLayout.setColspan(preprocessGrid, 2);

		// Setup preprocessGrid
		preprocessGrid.addStringColumn(CalendarSourcePreprocess::oldValue, CalendarSourcePreprocess::oldValue).setHeader("Regexp");
		preprocessGrid.addStringColumn(CalendarSourcePreprocess::newValue, CalendarSourcePreprocess::newValue).setHeader("Replacement");
		preprocessGrid.addCrudIconButtonbarColumn();
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

		binder.forField(scrapeBlockStartTextField).bind(CalendarSourceScraperBaseHTML::scrapeBlockStart, CalendarSourceScraperBaseHTML::scrapeBlockStart);
		binder.forField(scrapeBlockEndTextField).bind(CalendarSourceScraperBaseHTML::scrapeBlockEnd, CalendarSourceScraperBaseHTML::scrapeBlockEnd);
		binder.forField(removeCharsTextField).bind(CalendarSourceScraperBaseHTML::removeChars, CalendarSourceScraperBaseHTML::removeChars);
	}

	@Override
	public CalendarSourceScraperBaseHTMLForm populateWith(CalendarSource calendarSource) {
		super.populateWith(calendarSource);
		if (calendarSource instanceof CalendarSourceScraperBaseHTML calendarSourceScraperBaseHTML) {
			binder.readBean(calendarSourceScraperBaseHTML);
			preprocessGrid.setItems(calendarSourceScraperBaseHTML.calendarSourcePreprocesses());
		}
		return this;
	}

	@Override
	public CalendarSourceScraperBaseHTMLForm writeTo(CalendarSource calendarSource) throws ValidationException {
		super.writeTo(calendarSource);
		if (calendarSource instanceof CalendarSourceScraperBaseHTML calendarSourceScraperBaseHTML) {
			binder.writeBean(calendarSourceScraperBaseHTML);
			calendarSourceScraperBaseHTML.calendarSourcePreprocesses(preprocessGrid.getItems());
		}
		return this;
	}

}