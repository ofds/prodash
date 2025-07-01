package com.prodash.application.port.in;

public interface ScoreProposalsUseCase {

    /**
     * Analyzes and assigns an impact score to all unscored proposals
     * currently in the database.
     */
    void scoreProposals();
}