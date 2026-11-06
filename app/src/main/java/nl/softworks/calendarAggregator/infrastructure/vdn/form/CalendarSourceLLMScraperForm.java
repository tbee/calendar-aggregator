package nl.softworks.calendarAggregator.infrastructure.vdn.form;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.domain.entity.CalendarLocation;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceLLMScraper;
import nl.softworks.calendarAggregator.domain.interfaces.S;
import nl.softworks.calendarAggregator.domain.service.OpenRouterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarSourceLLMScraperForm extends CalendarSourceScraperBaseHtmlForm {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarSourceLLMScraperForm.class);

	private final Binder<CalendarSourceLLMScraper> binder = new Binder<>();

	private final ComboBox<OpenRouterService.Model> modelComboBox = new ComboBox<>("Model");
	private final TextArea promptTextArea = new TextArea("Prompt");

	public CalendarSourceLLMScraperForm() {

		modelComboBox.setItemLabelGenerator(model -> model == null ? "" : model.name());
		modelComboBox.setClearButtonVisible(true);
		modelComboBox.setTooltipText("The LLM used for scraping. Defaults to OpenRouter's auto model.");

		Button testButton = new Button("Test", evt -> test());

		FormLayout formLayout = addAsFormlayoutInAccordion("Prompt", modelComboBox, promptTextArea, testButton);
		formLayout.setColspan(modelComboBox, 2);
		formLayout.setColspan(promptTextArea, 2);
		formLayout.setColspan(testButton, 2);

		binder.forField(modelComboBox)
				.withConverter(
						model -> model == null ? OpenRouterService.AUTO_MODEL : model.id(),
						id -> modelComboBox.getListDataView().getItems()
								.filter(model -> model.id().equals(id))
								.findFirst()
								.orElse(null))
				.bind(CalendarSourceLLMScraper::model, CalendarSourceLLMScraper::model);
		binder.forField(promptTextArea).bind(CalendarSourceLLMScraper::prompt, CalendarSourceLLMScraper::prompt);
	}

	private void test() {
		CalendarSourceLLMScraper calendarSource = new CalendarSourceLLMScraper();
		new CalendarLocation().enabled(true).addCalendarSource(calendarSource);
		generateAndShowTrace(calendarSource);
	}

	@Override
	public CalendarSourceLLMScraperForm populateWith(CalendarSource calendarSource) {
		super.populateWith(calendarSource);
		if (calendarSource instanceof CalendarSourceLLMScraper calendarSourceLLMScraper) {
			modelComboBox.setItems(S.openRouterService().models());
			binder.readBean(calendarSourceLLMScraper);
		}
		return this;
	}

	@Override
	public CalendarSourceLLMScraperForm writeTo(CalendarSource calendarSource) throws ValidationException {
		super.writeTo(calendarSource);
		if (calendarSource instanceof CalendarSourceLLMScraper calendarSourceLLMScraper) {
			binder.writeBean(calendarSourceLLMScraper);
		}
		return this;
	}
}