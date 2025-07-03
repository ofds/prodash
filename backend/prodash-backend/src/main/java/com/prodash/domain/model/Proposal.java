package com.prodash.domain.model;

import java.time.LocalDate;

public class Proposal {

    private String id;
    private String title;
    private String siglaTipo; // ADDED
    private Integer numero;   // ADDED
    private Integer ano;      // ADDED
    private String summary;
    private String ementa;
    private String fullTextUrl;
    private String status;
    private LocalDate presentationDate;
    private Double impactScore;
    private String justification;

    // Constructors
    public Proposal() {
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

    public String getSiglaTipo() {
        return siglaTipo;
    }

    public void setSiglaTipo(String siglaTipo) {
        this.siglaTipo = siglaTipo;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getEmenta() {
        return ementa;
    }

    public void setEmenta(String ementa) {
        this.ementa = ementa;
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
                ", siglaTipo='" + siglaTipo + '\'' +
                ", numero=" + numero +
                ", ano=" + ano +
                ", summary='" + summary + '\'' +
                ", ementa='" + ementa + '\'' +
                ", fullTextUrl='" + fullTextUrl + '\'' +
                ", status='" + status + '\'' +
                ", presentationDate=" + presentationDate +
                ", impactScore=" + impactScore +
                ", justification='" + justification + '\'' +
                '}';
    }
}