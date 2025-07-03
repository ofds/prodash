package com.prodash.infrastructure.adapter.out.camara.dto;

// DTO para a resposta da API de detalhes do partido
public class CamaraPartyDTO {
    public int id;
    public String sigla;
    public String nome;

    // Classe aninhada para a estrutura de resposta da API
    public static class CamaraPartyResponse {
        public CamaraPartyDTO dados;
    }
}