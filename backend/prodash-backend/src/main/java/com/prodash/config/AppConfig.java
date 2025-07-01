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

        // Itera sobre os conversores de mensagens existentes
        for (int i = 0; i < restTemplate.getMessageConverters().size(); i++) {
            // Se encontrarmos o conversor de String predefinido...
            if (restTemplate.getMessageConverters().get(i) instanceof StringHttpMessageConverter) {
                // ...substituimo-lo por um novo que força UTF-8.
                restTemplate.getMessageConverters().set(i, new StringHttpMessageConverter(StandardCharsets.UTF_8));
                break; // Paramos após encontrar e substituir
            }
        }
        return restTemplate;
    }
}
