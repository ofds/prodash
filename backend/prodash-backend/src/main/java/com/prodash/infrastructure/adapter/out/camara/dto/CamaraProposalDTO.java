package com.prodash.infrastructure.adapter.out.camara.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CamaraProposalDTO {

    @SerializedName("id")
    private String id;
    @SerializedName("uri")
    private String uri; // ADDED
    @SerializedName("siglaTipo")
    private String siglaTipo;
    @SerializedName("descricaoTipo")
    private String descricaoTipo; // ADDED
    @SerializedName("numero")
    private int numero;
    @SerializedName("ano")
    private int ano;
    @SerializedName("ementa")
    private String ementa;
    @SerializedName("ementaDetalhada")
    private String ementaDetalhada; // ADDED
    @SerializedName("dataApresentacao")
    private String dataApresentacao;
    @SerializedName("urlInteiroTeor")
    private String urlInteiroTeor;
    @SerializedName("uriAutores")
    private String uriAutores; // ADDED
    @SerializedName("statusProposicao")
    private StatusProposicaoDTO statusProposicao;

    public static class CamaraApiResponse {
        public List<CamaraProposalDTO> dados;
        public List<LinkDTO> links;
    }

    public static class StatusProposicaoDTO {
        @SerializedName("dataHora")
        public String dataHora;
        @SerializedName("siglaOrgao")
        public String siglaOrgao;
        @SerializedName("descricaoTramitacao")
        public String descricaoTramitacao;
        @SerializedName("descricaoSituacao")
        public String descricaoSituacao; // ADDED
        @SerializedName("despacho")
        public String despacho;
    }

    public static class LinkDTO {
        @SerializedName("rel")
        public String rel;
        @SerializedName("href")
        public String href;
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getUri() { return uri; }
    public String getSiglaTipo() { return siglaTipo; }
    public String getDescricaoTipo() { return descricaoTipo; }
    public int getNumero() { return numero; }
    public int getAno() { return ano; }
    public String getEmenta() { return ementa; }
    public String getEmentaDetalhada() { return ementaDetalhada; }
    public String getDataApresentacao() { return dataApresentacao; }
    public String getUrlInteiroTeor() { return urlInteiroTeor; }
    public String getUriAutores() { return uriAutores; }
    public StatusProposicaoDTO getStatusProposicao() { return statusProposicao; }

    public String getTitle() {
        return String.format("%s %d/%d", siglaTipo, numero, ano);
    }
}