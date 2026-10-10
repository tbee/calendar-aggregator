package nl.softworks.calendarAggregator.application.http;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Calendar Aggregator API")
                        .description("REST API for the Calendar Aggregator")
                        .version("3.3.0"));
    }
}