package com.smartlogix.shipping.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartLogix - Despacho")
                        .version("1.0.0")
                        .description("Gestión de rutas de despacho, envíos y tracking. Soporte OSRM para optimización de rutas"));
    }
}
