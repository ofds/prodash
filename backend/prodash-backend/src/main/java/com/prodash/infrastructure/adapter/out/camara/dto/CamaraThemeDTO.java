package com.prodash.infrastructure.adapter.out.camara.dto;

import java.util.List;

public class CamaraThemeDTO {
    public int cod;
    public String nome;

    public static class CamaraThemeResponse {
        public List<CamaraThemeDTO> dados;
    }
}