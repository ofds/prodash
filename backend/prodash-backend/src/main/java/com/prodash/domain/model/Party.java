package com.prodash.domain.model;

/**
 * Representa um Partido Político.
 * @param id ID do Partido (e.g., 36779)
 * @param sigla Sigla do partido (e.g., "PCdoB")
 * @param nome Nome completo do partido.
 */
public record Party(
    Integer id,
    String sigla,
    String nome
) {}