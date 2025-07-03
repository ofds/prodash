package com.prodash.application.port.in;

/**
 * Defines the contract for a service that summarizes proposals.
 */
public interface SummarizeProposalsUseCase {

    /**
     * Triggers the process of finding proposals without summaries,
     * generating summaries for them using an LLM, and updating them in the database.
     */
    void summarizeUnsummarizedProposals();
}