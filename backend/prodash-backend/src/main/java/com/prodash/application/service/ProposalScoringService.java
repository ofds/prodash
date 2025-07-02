package com.prodash.application.service;

import com.google.common.collect.Lists;
import com.prodash.application.port.in.ScoreProposalsUseCase;
import com.prodash.application.port.out.LlmPort;
import com.prodash.application.port.out.ProposalRepositoryPort;
import com.prodash.domain.model.Proposal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProposalScoringService implements ScoreProposalsUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProposalScoringService.class);
    private static final int BATCH_SIZE = 10; // Added batch size

    private final ProposalRepositoryPort proposalRepositoryPort;
    private final LlmPort llmPort;

    public ProposalScoringService(ProposalRepositoryPort proposalRepositoryPort, LlmPort llmPort) {
        this.proposalRepositoryPort = proposalRepositoryPort;
        this.llmPort = llmPort;
    }

    @Override
    public void scoreProposals() {
        log.info("Starting proposal scoring process...");
        List<Proposal> unscoredProposals = proposalRepositoryPort.findByImpactScoreIsNull();

        if (unscoredProposals.isEmpty()) {
            log.info("No unscored proposals to process.");
            return;
        }

        log.info("Found {} unscored proposals. Scoring in batches of {}.", unscoredProposals.size(), BATCH_SIZE);

        // Partition the list into batches
        List<List<Proposal>> batches = Lists.partition(unscoredProposals, BATCH_SIZE);

        // Process each batch individually
        for (int i = 0; i < batches.size(); i++) {
            List<Proposal> batch = batches.get(i);
            log.info("Processing scoring batch {} of {}: {} proposals.", i + 1, batches.size(), batch.size());

            try {
                List<Proposal> scoredBatch = llmPort.scoreProposals(batch);
                scoredBatch.forEach(proposalRepositoryPort::save);
                log.info("Successfully scored and saved batch {} of {}.", i + 1, batches.size());
            } catch (Exception e) {
                log.error("Failed to process scoring batch {} of {}: {}", i + 1, batches.size(), e.getMessage());
            }
        }

        log.info("Finished proposal scoring process.");
    }
}