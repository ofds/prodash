package com.prodash.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "proposals")
public class Proposal {

    @Id
    private String id;
    private Long originalId;
    private String siglaTipo;
    private int numero;
    private int ano;
    private String ementa;

    // --- Fields added in the previous step ---
    private String justificativa;
    private String statusDescricao;

    // --- NEW FIELDS TO STORE MORE DETAILS ---
    private String dataApresentacao;
    private String descricaoTipo;
    private String urlInteiroTeor; // URL to the full text
    private String uriAutores; // URI to the list of authors

    // --- AI-Generated Fields ---
    private String summaryLLM;
    private String categoryLLM;
    private String impactoLLM;
    private Integer impactoScoreLLM;
    private String impactoJustificativaLLM;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}