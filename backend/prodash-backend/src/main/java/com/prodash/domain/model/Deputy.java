package com.prodash.domain.model;

/**
 * Representa um Deputado.
 * O 'record' gera automaticamente construtor, getters, equals, hashCode e toString.
 * @param id ID do Deputado (e.g., 220555)
 * @param nomeCivil Nome completo do deputado.
 * @param nomeParlamentar Nome parlamentar (e.g., "Daiana Santos")
 * @param siglaUf Sigla do estado (e.g., "RS")
 * @param partidoId Link para o ID do Partido.
 */
public record Deputy(
    Integer id,
    String nomeCivil,
    String nomeParlamentar,
    String siglaUf,
    Integer partidoId
) {}