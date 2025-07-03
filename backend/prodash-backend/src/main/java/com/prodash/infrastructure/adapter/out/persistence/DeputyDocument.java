package com.prodash.infrastructure.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "deputies")
public class DeputyDocument {
    @Id private Integer id;
    private String nomeCivil;
    private String nomeParlamentar;
    private String siglaUf;
    private Integer partidoId;

    // Getters e Setters (pode usar o Lombok se preferir)
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    // ... outros getters e setters
}