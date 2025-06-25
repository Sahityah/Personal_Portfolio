// src/main/java/com/Personal_Portfolio/Personal_Portfolio/Config/CorsConfig.java
package com.Personal_Portfolio.Personal_Portfolio.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Apply CORS to all paths in your API
                        .allowedOrigins("https://portpolio-core-front.onrender.com") // !!! IMPORTANT: Your frontend domain !!!
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // <--- Added OPTIONS method
                        .allowedHeaders("*") // Allow all headers
                        .allowCredentials(true) // <--- Added to allow credentials (e.g., Authorization headers, cookies)
                        .maxAge(3600); // How long the preflight request results can be cached (in seconds)
            }
        };
    }
}
