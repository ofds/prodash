package com.prodash.application.port.in;

import java.util.List;

public interface FetchProposalsUseCase {

    /**
     * Triggers the process of fetching new legislative proposal IDs from an external source,
     * retrieving their details, and storing them as raw data in the local database.
     */
    List<String> fetchNewProposals();
}