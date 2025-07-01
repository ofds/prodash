package com.prodash.application.service;

import com.prodash.application.port.in.ScoreProposalsUseCase;
import com.prodash.application.port.out.LlmPort;
import com.prodash.application.port.out.ProposalRepositoryPort;
import com.prodash.domain.model.Proposal;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProposalScoringService implements ScoreProposalsUseCase {

    private final ProposalRepositoryPort proposalRepositoryPort;
    private final LlmPort llmPort;

    public ProposalScoringService(ProposalRepositoryPort proposalRepositoryPort, LlmPort llmPort) {
        this.proposalRepositoryPort = proposalRepositoryPort;
        this.llmPort = llmPort;
    }

    // In ProposalScoringService.java
    @Override
    public void scoreProposals() {
        System.out.println("Starting proposal scoring process...");
        List<Proposal> unscoredProposals = proposalRepositoryPort.findByImpactScoreIsNull();

        if (unscoredProposals.isEmpty()) {
            System.out.println("No unscored proposals to process.");
            return;
        }

        System.out.println("Found " + unscoredProposals.size() + " unscored proposals. Scoring...");

        // Score the entire batch of unscored proposals
        List<Proposal> scoredProposals = llmPort.scoreProposals(unscoredProposals);

        // Save all scored proposals
        scoredProposals.forEach(proposalRepositoryPort::save);

        System.out.println("Successfully scored and updated " + scoredProposals.size() + " proposals.");
    }
}