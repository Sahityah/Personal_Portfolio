package com.Personal_Portfolio.Personal_Portfolio.Config;


import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI personalPortfolioOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Personal Portfolio API")
                        .description("API documentation for Personal Portfolio Application")
                        .version("1.0.0")
                        .contact(new Contact().name("Sahitya").email("yourmail@gmail.com")).
                        license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local server")
                ) );
    }
}

