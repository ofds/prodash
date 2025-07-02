// src/main/java/com/prodash/infrastructure/adapter/out/llm/dto/AnalysisPayload.java
package com.prodash.infrastructure.adapter.out.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the payload sent for analysis to the LLM.
 * This class is serialized into the 'content' field of the API request.
 */
@Getter
@AllArgsConstructor
public class AnalysisPayload {

    /** The unique identifier for the proposal being analyzed. */
    private String proposalId;

    /** The summary text (ementa) of the proposal to be analyzed. */
    private String ementa;
}