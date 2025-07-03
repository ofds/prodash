package com.prodash.infrastructure.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "parties")
public class PartyDocument {
    @Id private Integer id;
    private String sigla;
    private String nome;
    
    // Getters e Setters
}