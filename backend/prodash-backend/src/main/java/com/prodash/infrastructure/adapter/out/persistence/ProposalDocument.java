package com.prodash.infrastructure.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Document(collection = "proposals")
public class ProposalDocument {

    @Id
    private String id;
    private String uri;
    private String title;
    @Field("sigla_tipo")
    private String siglaTipo;
    @Field("descricao_tipo")
    private String descricaoTipo;
    private Integer numero;
    private Integer ano;
    private String summary;
    private String ementa;
    @Field("ementa_detalhada")
    private String ementaDetalhada;
    @Field("full_text_url")
    private String fullTextUrl;
    @Field("uri_autores")
    private String uriAutores;
    private String status;
    private String situation;
    @Field("presentation_date")
    private LocalDate presentationDate;
    @Field("impact_score")
    private Double impactScore;
    private String justification;
    private String dispatch;
    @Field("processing_agency")
    private String processingAgency;
    private List<AuthorDocument> authors;
    private List<ThemeDocument> themes;

}