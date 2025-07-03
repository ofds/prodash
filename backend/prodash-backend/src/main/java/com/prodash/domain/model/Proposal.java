package com.prodash.domain.model;

import java.time.LocalDate;
import java.util.List;

public class Proposal {

    private String id;
    private String uri;
    private String title;
    private String siglaTipo;
    private String descricaoTipo;
    private Integer numero;
    private Integer ano;
    private String summary;
    private String ementa;
    private String ementaDetalhada;
    private String fullTextUrl;
    private String uriAutores;
    private String status;
    private String situation; // Maps to descricaoSituacao
    private LocalDate presentationDate;
    private Double impactScore;
    private String justification;
    private String dispatch;
    private String processingAgency;
    private List<Author> authors;

    // Getters and Setters for all fields...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSiglaTipo() { return siglaTipo; }
    public void setSiglaTipo(String siglaTipo) { this.siglaTipo = siglaTipo; }
    public String getDescricaoTipo() { return descricaoTipo; }
    public void setDescricaoTipo(String descricaoTipo) { this.descricaoTipo = descricaoTipo; }
    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getEmenta() { return ementa; }
    public void setEmenta(String ementa) { this.ementa = ementa; }
    public String getEmentaDetalhada() { return ementaDetalhada; }
    public void setEmentaDetalhada(String ementaDetalhada) { this.ementaDetalhada = ementaDetalhada; }
    public String getFullTextUrl() { return fullTextUrl; }
    public void setFullTextUrl(String fullTextUrl) { this.fullTextUrl = fullTextUrl; }
    public String getUriAutores() { return uriAutores; }
    public void setUriAutores(String uriAutores) { this.uriAutores = uriAutores; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSituation() { return situation; }
    public void setSituation(String situation) { this.situation = situation; }
    public LocalDate getPresentationDate() { return presentationDate; }
    public void setPresentationDate(LocalDate presentationDate) { this.presentationDate = presentationDate; }
    public Double getImpactScore() { return impactScore; }
    public void setImpactScore(Double impactScore) { this.impactScore = impactScore; }
    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
    public String getDispatch() { return dispatch; }
    public void setDispatch(String dispatch) { this.dispatch = dispatch; }
    public String getProcessingAgency() { return processingAgency; }
    public void setProcessingAgency(String processingAgency) { this.processingAgency = processingAgency; }
    public List<Author> getAuthors() { return authors; }
    public void setAuthors(List<Author> authors) { this.authors = authors; }
}