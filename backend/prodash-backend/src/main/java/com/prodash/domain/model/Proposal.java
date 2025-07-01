package com.prodash.domain.model;

import java.time.LocalDate;

public class Proposal {

    private String id;
    private String title;
    private String summary;
    private String fullTextUrl;
    private String status;
    private LocalDate presentationDate;
    private Double impactScore;
    private String justification;

    // Constructors
    public Proposal() {
    }

    public Proposal(String id, String title, String summary, String fullTextUrl, String status, LocalDate presentationDate, Double impactScore, String justification) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.fullTextUrl = fullTextUrl;
        this.status = status;
        this.presentationDate = presentationDate;
        this.impactScore = impactScore;
        this.justification = justification;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getFullTextUrl() {
        return fullTextUrl;
    }

    public void setFullTextUrl(String fullTextUrl) {
        this.fullTextUrl = fullTextUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getPresentationDate() {
        return presentationDate;
    }

    public void setPresentationDate(LocalDate presentationDate) {
        this.presentationDate = presentationDate;
    }

    public Double getImpactScore() {
        return impactScore;
    }

    public void setImpactScore(Double impactScore) {
        this.impactScore = impactScore;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    // toString method for logging and debugging
    @Override
    public String toString() {
        return "Proposal{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", summary='" + summary + '\'' +
                ", fullTextUrl='" + fullTextUrl + '\'' +
                ", status='" + status + '\'' +
                ", presentationDate=" + presentationDate +
                ", impactScore=" + impactScore +
                ", justification='" + justification + '\'' +
                '}';
    }
}