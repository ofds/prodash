package com.prodash.infrastructure.adapter.out.llm.dto;

import com.google.gson.annotations.SerializedName;

public class LlmResult {
    private String id;

    // ADDED: The echoed correlation ID from the LLM
    @SerializedName("correlation_id")
    private String correlationId;

    @SerializedName("impact_score")
    private Double impactScore;
    
    private String justification;
    private String summary;

    // Getters
    public String getId() { return id; }
    public String getCorrelationId() { return correlationId; } // Getter for the new field
    public Double getImpactScore() { return impactScore; }
    public String getJustification() { return justification; }
    public String getSummary() { return summary; }
}