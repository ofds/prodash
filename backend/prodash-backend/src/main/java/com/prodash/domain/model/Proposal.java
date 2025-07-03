package com.prodash.domain.model;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
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
    private List<Theme> themes;
}