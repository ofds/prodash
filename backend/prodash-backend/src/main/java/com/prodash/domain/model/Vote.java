package com.prodash.domain.model;

/**
 * Representa o voto individual de um Deputado em uma Votação.
 * @param votacaoId Link para a Votação.
 * @param deputadoId Link para o Deputado.
 * @param tipoVoto Voto registrado (e.g., "Sim", "Não", "Abstenção").
 */
public record Vote(
    String votacaoId,
    Integer deputadoId,
    String tipoVoto
) {}