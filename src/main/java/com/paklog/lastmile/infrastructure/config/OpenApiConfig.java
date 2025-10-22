package com.paklog.lastmile.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lastMileDeliveryOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Last-Mile Delivery Coordination API")
                .description("Last-mile delivery coordination for Paklog WMS/WES platform")
                .version("1.0.0"));
    }
}
