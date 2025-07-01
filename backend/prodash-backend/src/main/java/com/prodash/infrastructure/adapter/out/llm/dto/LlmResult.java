// in LlmResult.java
package com.prodash.infrastructure.adapter.out.llm.dto;
public class LlmResult {
    private String id;
    private Double impact_score;
    private String justification;
    private String summary;
    // Getters
    public String getId() { return id; }
    public Double getImpactScore() { return impact_score; }
    public String getJustification() { return justification; }
    public String getSummary() { return summary; }
}