package com.prodash.infrastructure.adapter.out.camara.dto;

import com.google.gson.annotations.SerializedName;

// This DTO represents a single proposal item from the Camara API response.
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
        public CamaraProposalDTO[] dados;
    }
    
    // --- Getters ---
    
    public String getId() {
        return id;
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