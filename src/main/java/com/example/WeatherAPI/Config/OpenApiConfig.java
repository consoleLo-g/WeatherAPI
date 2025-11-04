package com.example.WeatherAPI.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI weatherApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Weather API")
                        .description("API for fetching weather details by latitude and longitude.")
                        .version("1.0"));
    }
}
