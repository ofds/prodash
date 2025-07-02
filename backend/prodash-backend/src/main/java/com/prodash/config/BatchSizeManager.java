package com.prodash.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages the dynamic batch size for API calls to the LLM.
 * This component is thread-safe and allows for runtime adjustments
 * to balance performance and reliability.
 */
@Component
public class BatchSizeManager {

    private final AtomicInteger currentBatchSize;
    private final int minBatchSize;
    private final int maxBatchSize;

    public BatchSizeManager(
            @Value("${llm.batch.initial-size:50}") int initialBatchSize,
            @Value("${llm.batch.min-size:5}") int minBatchSize,
            @Value("${llm.batch.max-size:100}") int maxBatchSize
    ) {
        this.minBatchSize = minBatchSize;
        this.maxBatchSize = maxBatchSize;
        this.currentBatchSize = new AtomicInteger(initialBatchSize);
    }

    /**
     * Gets the current batch size to be used for processing.
     * @return The current batch size.
     */
    public int getBatchSize() {
        return currentBatchSize.get();
    }

    /**
     * Decreases the batch size when API throttling or errors occur.
     * The size is halved, but will not go below the configured minimum.
     */
    public void decreaseBatchSize() {
        int newSize = currentBatchSize.updateAndGet(size -> Math.max(minBatchSize, size / 2));
        System.out.println("API throttling detected. Reducing batch size to " + newSize);
    }

    /**
     * Gradually increases the batch size after a period of successful operations.
     * The size increases by a small, fixed step, up to the configured maximum.
     */
    public void increaseBatchSize() {
        int newSize = currentBatchSize.updateAndGet(size -> Math.min(maxBatchSize, size + 5));
        if (newSize > currentBatchSize.get()) {
             System.out.println("Operation successful. Increasing batch size to " + newSize);
        }
    }
}