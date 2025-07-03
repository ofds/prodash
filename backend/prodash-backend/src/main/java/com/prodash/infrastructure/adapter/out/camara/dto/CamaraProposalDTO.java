package com.prodash.infrastructure.adapter.out.camara.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CamaraProposalDTO {

    @SerializedName("id")
    private String id;

    @SerializedName("siglaTipo")
    private String siglaTipo;

    @SerializedName("numero")
    private int numero;
    
    @SerializedName("ano")
    private int ano;

    @SerializedName("ementa")
    private String ementa;
    
    @SerializedName("urlInteiroTeor")
    private String urlInteiroTeor;

    // This nested class represents the overall API response structure.
    public static class CamaraApiResponse {
        public List<CamaraProposalDTO> dados; 
        public List<LinkDTO> links; 
    }
    
    // This nested class represents the link object needed for pagination
    public static class LinkDTO {
        @SerializedName("rel")
        public String rel;

        @SerializedName("href")
        public String href;
    }
    
    // --- Getters ---
    
    public String getId() {
        return id;
    }

    public String getSiglaTipo() {
        return siglaTipo;
    }

    public int getNumero() {
        return numero;
    }

    public int getAno() {
        return ano;
    }
    
    public String getEmenta() {
        return ementa;
    }

    public String getUrlInteiroTeor() {
        return urlInteiroTeor;
    }

    // A helper method to create a user-friendly title
    public String getTitle() {
        return String.format("%s %d/%d", siglaTipo, numero, ano);
    }
}