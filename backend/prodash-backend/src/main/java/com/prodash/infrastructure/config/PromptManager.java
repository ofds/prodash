package com.prodash.infrastructure.config;

import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class PromptManager {

    public String getPrompt(String promptName) {
        try (InputStream inputStream = getClass().getResourceAsStream("/prompts/" + promptName + ".txt")) {
            if (inputStream == null) {
                throw new RuntimeException("Prompt file not found: " + promptName);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read prompt: " + promptName, e);
        }
    }
}