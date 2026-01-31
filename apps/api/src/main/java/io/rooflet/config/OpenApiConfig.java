package io.rooflet.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI roofletOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rooflet API")
                        .description("Backend API for Rooflet Landloard Management System")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@doesnotexist.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
