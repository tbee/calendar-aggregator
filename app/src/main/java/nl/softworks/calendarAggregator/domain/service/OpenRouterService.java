package nl.softworks.calendarAggregator.domain.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenRouterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenRouterService.class);

    /** OpenRouter's automatic model selection; used as the default. */
    public static final String AUTO_MODEL = "openrouter/auto";

    public record Model(String id, String name) {
    }

    @Value("${spring.ai.openai.base-url:https://openrouter.ai/api/v1}")
    private String baseUrl;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    private List<Model> cachedModels;

    /**
     * All available models, sorted by name. The result is cached after the first
     * (successful) call, as the list changes rarely and fetching is a network call.
     */
    public List<Model> models() {
        if (cachedModels == null) {
            cachedModels = fetchModels();
        }
        return cachedModels;
    }

    private List<Model> fetchModels() {
        // Keep insertion order + dedupe by id, always starting with the auto model.
        Map<String, Model> modelsById = new LinkedHashMap<>();
        modelsById.put(AUTO_MODEL, new Model(AUTO_MODEL, "Auto (let OpenRouter pick)"));

        try {
            ModelsResponse response = RestClient.create()
                    .get()
                    .uri(baseUrl + "/models")
                    .headers(headers -> {
                        if (apiKey != null && !apiKey.isBlank()) {
                            headers.setBearerAuth(apiKey);
                        }
                    })
                    .retrieve()
                    .body(ModelsResponse.class);

            if (response != null && response.data() != null) {
                for (ModelData modelData : response.data()) {
                    if (modelData.id() == null || modelData.id().isBlank()) {
                        continue;
                    }
                    String name = (modelData.name() == null || modelData.name().isBlank()) ? modelData.id() : modelData.name();
                    modelsById.putIfAbsent(modelData.id(), new Model(modelData.id(), name));
                }
            }
        }
        catch (RuntimeException e) {
            LOGGER.warn("Could not fetch models from OpenRouter, only the auto model is available", e);
        }

        List<Model> models = new ArrayList<>(modelsById.values());
        models.sort(Comparator.comparing(Model::name, String.CASE_INSENSITIVE_ORDER));
        return models;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ModelsResponse(List<ModelData> data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ModelData(String id, String name) {
    }
}
