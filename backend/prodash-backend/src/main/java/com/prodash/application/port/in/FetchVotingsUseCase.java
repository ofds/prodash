package com.prodash.application.port.in;

import java.util.List;

public interface FetchVotingsUseCase {
    void fetchNewVotingsForProposals(List<String> proposalIds);
}