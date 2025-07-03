package com.prodash.infrastructure.adapter.out.camara.dto;

// DTO para a resposta da API de detalhes do deputado
public class CamaraDeputyDTO {
    public int id;
    public String nomeCivil;
    public UltimoStatusDTO ultimoStatus;

    public static class UltimoStatusDTO {
        public String nome; // Nome parlamentar
        public String siglaUf;
        public String siglaPartido;
        public String urlFoto;
        public int idLegislatura;
        // O ID do partido não vem aqui, teremos que obtê-lo de outra forma se necessário,
        // mas para o nosso fluxo, o `siglaPartido` é suficiente para encontrar o ID do partido.
    }

    // Classe aninhada para a estrutura de resposta da API
    public static class CamaraDeputyResponse {
        public CamaraDeputyDTO dados;
    }
}