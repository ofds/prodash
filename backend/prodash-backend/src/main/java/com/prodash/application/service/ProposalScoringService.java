package com.prodash.application.service;

import com.prodash.application.port.in.ScoreProposalsUseCase;
import com.prodash.application.port.out.LlmPort;
import com.prodash.application.port.out.ProposalRepositoryPort;
import com.prodash.domain.model.Proposal;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProposalScoringService implements ScoreProposalsUseCase {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProposalScoringService.class);

    private final ProposalRepositoryPort proposalRepositoryPort;
    private final LlmPort llmPort;

    public ProposalScoringService(ProposalRepositoryPort proposalRepositoryPort, LlmPort llmPort) {
        this.proposalRepositoryPort = proposalRepositoryPort;
        this.llmPort = llmPort;
    }

    // In ProposalScoringService.java
    @Override
    public void scoreProposals() {
        log.info("Starting proposal scoring process...");
        List<Proposal> unscoredProposals = proposalRepositoryPort.findByImpactScoreIsNull();

        if (unscoredProposals.isEmpty()) {
            log.info("No unscored proposals to process.");
            return;
        }

        log.info("Found {} unscored proposals. Scoring...", unscoredProposals.size());

        // Score the entire batch of unscored proposals
        List<Proposal> scoredProposals = llmPort.scoreProposals(unscoredProposals);

        // Save all scored proposals
        scoredProposals.forEach(proposalRepositoryPort::save);

        log.info("Successfully scored and updated {} proposals.", scoredProposals.size());
    }
}