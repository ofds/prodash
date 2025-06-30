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

    // --- AI-Generated Fields ---
    private String summaryLLM;
    private String categoryLLM;
    private String impactoLLM; // [NEW] The new impact analysis field

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}