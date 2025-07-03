package com.prodash.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "votes")
public class VoteDocument {
    private String votacaoId;
    private Integer deputadoId;
    private String tipoVoto;

    // Getters e Setters
}