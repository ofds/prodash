package com.prodash.infrastructure.adapter.out.camara.dto;

import java.util.List;

// DTO para a resposta da API de votos individuais
public class CamaraVoteDTO {
    public String tipoVoto;
    public DeputadoResumidoDTO deputado_; // O nome do campo na API é "deputado_"

    // DTO para os dados resumidos do deputado dentro do voto
    public static class DeputadoResumidoDTO {
        public int id;
    }

    // Classe aninhada para a estrutura de resposta da API
    public static class CamaraVoteResponse {
        public List<CamaraVoteDTO> dados;
    }
}