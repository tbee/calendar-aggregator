package nl.softworks.calendarAggregator.infrastructure.vdn.form;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import nl.softworks.calendarAggregator.domain.entity.CalendarLocation;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceAIAgentScraper;
import nl.softworks.calendarAggregator.domain.interfaces.S;
import nl.softworks.calendarAggregator.domain.service.OpenRouterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CalendarSourceAIAgentScraperForm extends CalendarSourceForm {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarSourceAIAgentScraperForm.class);

	private final Binder<CalendarSourceAIAgentScraper> binder = new Binder<>();

	private final ComboBox<OpenRouterService.Model> modelComboBox = new ComboBox<>("Model");
	private final TextArea promptTextArea = new TextArea("Prompt");

	public CalendarSourceAIAgentScraperForm() {

		modelComboBox.setItemLabelGenerator(model -> model == null ? "" : model.name());
		modelComboBox.setClearButtonVisible(true);
		modelComboBox.setTooltipText("The LLM that drives the browser. Only models that advertise tool calling are listed. Defaults to DeepSeek.");

		Button testButton = new Button("Test", evt -> test());

		FormLayout formLayout = addAsFormlayoutInAccordion("Prompt", modelComboBox, promptTextArea, testButton);
		formLayout.setColspan(modelComboBox, 2);
		formLayout.setColspan(promptTextArea, 2);
		formLayout.setColspan(testButton, 2);

		binder.forField(modelComboBox)
				.withConverter(
						model -> model == null ? OpenRouterService.DEFAULT_AGENT_MODEL : model.id(),
						id -> modelComboBox.getListDataView().getItems()
								.filter(model -> model.id().equals(id))
								.findFirst()
								.orElse(null))
				.bind(CalendarSourceAIAgentScraper::model, CalendarSourceAIAgentScraper::model);
		binder.forField(promptTextArea).bind(CalendarSourceAIAgentScraper::prompt, CalendarSourceAIAgentScraper::prompt);
	}

	private void test() {
		CalendarSourceAIAgentScraper calendarSource = new CalendarSourceAIAgentScraper();
		new CalendarLocation().enabled(true).addCalendarSource(calendarSource);
		generateAndShowTrace(calendarSource);
	}

	@Override
	public CalendarSourceAIAgentScraperForm populateWith(CalendarSource calendarSource) {
		super.populateWith(calendarSource);
		if (calendarSource instanceof CalendarSourceAIAgentScraper calendarSourceAIAgentScraper) {
			List<OpenRouterService.Model> models = new ArrayList<>(S.openRouterService().models().stream()
					.filter(OpenRouterService.Model::supportsTools)
					.toList());
			String selectedModelId = calendarSourceAIAgentScraper.model();
			if (selectedModelId != null && models.stream().noneMatch(model -> model.id().equals(selectedModelId))) {
				// Keep an existing (possibly tool-less) selection visible instead of blanking the field
				models.add(0, new OpenRouterService.Model(selectedModelId, selectedModelId + " (tool calling not advertised)", false));
			}
			modelComboBox.setItems(models);
			binder.readBean(calendarSourceAIAgentScraper);
		}
		return this;
	}

	@Override
	public CalendarSourceAIAgentScraperForm writeTo(CalendarSource calendarSource) throws ValidationException {
		super.writeTo(calendarSource);
		if (calendarSource instanceof CalendarSourceAIAgentScraper calendarSourceAIAgentScraper) {
			binder.writeBean(calendarSourceAIAgentScraper);
		}
		return this;
	}
}