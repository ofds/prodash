package com.prodash.infrastructure.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "votings")
public class VotingDocument {
    @Id private String id;
    private Integer proposicaoId;
    private LocalDate data;
    private String resumo;

    // Getters e Setters
}