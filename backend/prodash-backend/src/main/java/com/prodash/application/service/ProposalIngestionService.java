package com.prodash.application.service;

import com.google.common.collect.Lists;
import com.prodash.application.port.in.IngestProposalsUseCase;
import com.prodash.application.port.out.CamaraApiPort;
import com.prodash.application.port.out.LlmPort;
import com.prodash.application.port.out.ProposalRepositoryPort;
import com.prodash.domain.model.Proposal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProposalIngestionService implements IngestProposalsUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProposalIngestionService.class);
    private static final int BATCH_SIZE = 10;

    private final CamaraApiPort camaraApiPort;
    private final ProposalRepositoryPort proposalRepositoryPort;
    private final LlmPort llmPort;

    public ProposalIngestionService(CamaraApiPort camaraApiPort, ProposalRepositoryPort proposalRepositoryPort,
                                    LlmPort llmPort) {
        this.camaraApiPort = camaraApiPort;
        this.proposalRepositoryPort = proposalRepositoryPort;
        this.llmPort = llmPort;
    }

    @Override
    public void ingestProposals() {
        log.info("Starting proposal ingestion process...");
        List<Proposal> fetchedProposals = camaraApiPort.fetchLatestProposals();

        List<Proposal> newProposals = fetchedProposals.stream()
                .filter(p -> proposalRepositoryPort.findById(p.getId()).isEmpty())
                .collect(Collectors.toList());

        if (newProposals.isEmpty()) {
            log.info("No new proposals to ingest.");
            return;
        }

        log.info("Found {} new proposals. Summarizing and saving in batches of {}.", newProposals.size(), BATCH_SIZE);

        List<List<Proposal>> batches = Lists.partition(newProposals, BATCH_SIZE);

        for (int i = 0; i < batches.size(); i++) {
            List<Proposal> batch = batches.get(i);
            log.info("Processing batch {} of {}: {} proposals.", i + 1, batches.size(), batch.size());

            try {
                List<Proposal> summarizedProposals = llmPort.summarizeProposals(batch);
                summarizedProposals.forEach(proposalRepositoryPort::save);
                log.info("Successfully summarized and saved batch {} of {}.", i + 1, batches.size());
            } catch (Exception e) {
                log.error("Failed to process batch {} of {}: {}", i + 1, batches.size(), e.getMessage());
            }
        }

        log.info("Finished proposal ingestion process.");
    }
}