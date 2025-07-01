package com.prodash.application.service;

import com.prodash.application.port.in.IngestProposalsUseCase;
import com.prodash.application.port.out.CamaraApiPort;
import com.prodash.application.port.out.LlmPort;
import com.prodash.application.port.out.ProposalRepositoryPort;
import com.prodash.domain.model.Proposal;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProposalIngestionService implements IngestProposalsUseCase {

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
        System.out.println("Starting proposal ingestion process...");
        List<Proposal> fetchedProposals = camaraApiPort.fetchLatestProposals();

        // Filter out proposals that already exist
        List<Proposal> newProposals = fetchedProposals.stream()
                .filter(p -> proposalRepositoryPort.findById(p.getId()).isEmpty())
                .collect(Collectors.toList());

        if (newProposals.isEmpty()) {
            System.out.println("No new proposals to ingest.");
            return;
        }

        System.out.println("Found " + newProposals.size() + " new proposals. Summarizing...");

        // Summarize the entire batch of new proposals
        List<Proposal> summarizedProposals = llmPort.summarizeProposals(newProposals);

        // Save all summarized proposals
        summarizedProposals.forEach(proposalRepositoryPort::save);

        System.out.println("Successfully ingested and saved " + summarizedProposals.size() + " proposals.");
    }
}