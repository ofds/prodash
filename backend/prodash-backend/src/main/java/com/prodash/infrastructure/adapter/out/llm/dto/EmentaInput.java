package com.prodash.infrastructure.adapter.out.llm.dto;

public class EmentaInput {
    private String id;
    private String ementa;
    private String correlationId; // ADDED: Redundant identifier

    public EmentaInput(String id, String ementa, String correlationId) {
        this.id = id;
        this.ementa = ementa;
        this.correlationId = correlationId;
    }
    // Getters are not strictly needed by Gson for serialization
}