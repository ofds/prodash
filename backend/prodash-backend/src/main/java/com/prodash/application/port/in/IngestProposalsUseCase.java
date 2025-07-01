package com.prodash.application.port.in;

public interface IngestProposalsUseCase {

    /**
     * Triggers the process of ingesting legislative proposals from an external source,
     * such as the Câmara API, and stores them in the local database.
     */
    void ingestProposals();
}