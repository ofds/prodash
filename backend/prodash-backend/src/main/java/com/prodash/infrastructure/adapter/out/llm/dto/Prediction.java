package com.prodash.infrastructure.adapter.out.llm.dto;

import com.google.gson.annotations.SerializedName;

public class Prediction {
    private String content;
    private SafetyAttributes safetyAttributes;
    @SerializedName("citationMetadata")
    private CitationMetadata citationMetadata;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public SafetyAttributes getSafetyAttributes() {
        return safetyAttributes;
    }

    public void setSafetyAttributes(SafetyAttributes safetyAttributes) {
        this.safetyAttributes = safetyAttributes;
    }

    public CitationMetadata getCitationMetadata() {
        return citationMetadata;
    }

    public void setCitationMetadata(CitationMetadata citationMetadata) {
        this.citationMetadata = citationMetadata;
    }
}