package com.prodash.config;

import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * General application configuration beans.
 */
@Configuration
public class AppConfig {

    /**
     * Provides a singleton RestTemplate bean to be used across the application
     * for making HTTP requests.
     * @return A RestTemplate instance.
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // Add a message converter that forces UTF-8 encoding for all strings.
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }
}
