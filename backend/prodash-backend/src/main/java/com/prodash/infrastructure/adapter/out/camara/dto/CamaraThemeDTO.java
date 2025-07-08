package com.prodash.infrastructure.adapter.out.camara.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class CamaraThemeDTO {
    @SerializedName("codTema")
    public int cod;

    @SerializedName("tema")
    public String nome;

    public static class CamaraThemeResponse {
        public List<CamaraThemeDTO> dados;
    }
}