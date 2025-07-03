package com.prodash.domain.model;

import java.time.LocalDate;

/**
 * Representa uma sessão de votação.
 * @param id ID da Votação (e.g., "2383019-91")
 * @param proposicaoId Link para a Proposição.
 * @param data Data da votação.
 * @param resumo A descrição do que foi votado.
 */
public record Voting(
    String id,
    Integer proposicaoId,
    LocalDate data,
    String resumo
) {}