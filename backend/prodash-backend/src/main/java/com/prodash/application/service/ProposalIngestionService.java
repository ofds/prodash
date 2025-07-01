package com.prodash.application.service;

import com.prodash.application.port.in.IngestProposalsUseCase;
import com.prodash.application.port.out.CamaraApiPort;
import com.prodash.application.port.out.LlmPort;
import com.prodash.application.port.out.ProposalRepositoryPort;
import com.prodash.domain.model.Proposal;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProposalIngestionService implements IngestProposalsUseCase {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProposalIngestionService.class);

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

        // Filter out proposals that already exist
        List<Proposal> newProposals = fetchedProposals.stream()
                .filter(p -> proposalRepositoryPort.findById(p.getId()).isEmpty())
                .collect(Collectors.toList());

        if (newProposals.isEmpty()) {
            log.info("No new proposals to ingest.");
            return;
        }

        log.info("Found {} new proposals. Summarizing...", newProposals.size());

        // Summarize the entire batch of new proposals
        List<Proposal> summarizedProposals = llmPort.summarizeProposals(newProposals);

        // Save all summarized proposals
        summarizedProposals.forEach(proposalRepositoryPort::save);

        log.info("Successfully ingested and saved {} proposals.", summarizedProposals.size());
    }
}