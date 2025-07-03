package com.prodash.infrastructure.adapter.out.camara.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CamaraAuthorDTO {

    @SerializedName("nome")
    private String nome;

    @SerializedName("tipo")
    private String tipo;

    // --- Getters ---
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }

    // --- Nested class for the API response wrapper ---
    public static class CamaraAuthorResponse {
        public List<CamaraAuthorDTO> dados;
    }
}