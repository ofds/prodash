package com.prodash.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages loading and providing LLM prompts from files in the classpath.
 */
@Service
public class PromptManager {

    private static final Logger logger = LoggerFactory.getLogger(PromptManager.class);
    private static final String PROMPTS_PATH = "classpath:prompts/*.txt";
    private final Map<String, String> promptCache = new ConcurrentHashMap<>();

    /**
     * This method runs once after the service is created.
     * It scans the prompts directory and loads all .txt files into memory.
     */
    @PostConstruct
    public void init() {
        logger.info("Initializing PromptManager and loading prompts...");
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(PROMPTS_PATH);
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename != null) {
                    String promptName = filename.replace(".txt", "");
                    String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    promptCache.put(promptName, content);
                    logger.info("Loaded prompt: '{}'", promptName);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load prompts from path: {}", PROMPTS_PATH, e);
            throw new RuntimeException("Could not initialize PromptManager", e);
        }
        logger.info("PromptManager initialized successfully with {} prompts.", promptCache.size());
    }

    /**
     * Retrieves a prompt by its name (which is its filename without the .txt extension).
     * @param name The name of the prompt to retrieve.
     * @return The full text of the prompt.
     * @throws IllegalArgumentException if the prompt is not found.
     */
    public String getPrompt(String name) {
        String prompt = promptCache.get(name);
        if (prompt == null) {
            throw new IllegalArgumentException("Prompt not found: " + name);
        }
        return prompt;
    }
}
