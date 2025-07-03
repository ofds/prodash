package com.prodash.domain.model;

/**
 * Representa o autor de uma proposição. Imutável.
 * @param name Nome do autor.
 * @param type Tipo do autor (e.g., "Deputado(a)").
 */
public record Author(String name, String type) {}