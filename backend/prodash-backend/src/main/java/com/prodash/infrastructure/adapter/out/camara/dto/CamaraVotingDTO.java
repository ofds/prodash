package com.prodash.infrastructure.adapter.out.camara.dto;

import java.time.LocalDate;
import java.util.List;

// DTO para a resposta da API de votações
public class CamaraVotingDTO {
    public String id;
    public LocalDate data;
    public String descricao;

    // Classe aninhada para a estrutura de resposta da API
    public static class CamaraVotingResponse {
        public List<CamaraVotingDTO> dados;
    }
}