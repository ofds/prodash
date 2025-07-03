package com.prodash.infrastructure.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDate;

@Document(collection = "proposals")
public class ProposalDocument {

    @Id
    private String id;
    private String title;
    private String summary;
    private String ementa; // Add this line
    @Field("full_text_url") // Example of mapping to a different field name
    private String fullTextUrl;
    private String status;
    @Field("presentation_date")
    private LocalDate presentationDate;
    @Field("impact_score")
    private Double impactScore;
    private String justification;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getEmenta() { return ementa; }
    public void setEmenta(String ementa) { this.ementa = ementa; }
    public String getFullTextUrl() { return fullTextUrl; }
    public void setFullTextUrl(String fullTextUrl) { this.fullTextUrl = fullTextUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getPresentationDate() { return presentationDate; }
    public void setPresentationDate(LocalDate presentationDate) { this.presentationDate = presentationDate; }
    public Double getImpactScore() { return impactScore; }
    public void setImpactScore(Double impactScore) { this.impactScore = impactScore; }
    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
}