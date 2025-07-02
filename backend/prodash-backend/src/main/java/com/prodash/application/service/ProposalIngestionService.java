package com.prodash.application.service;

import com.google.common.collect.Lists;
import com.prodash.application.port.in.IngestProposalsUseCase;
import com.prodash.application.port.out.CamaraApiPort;
import com.prodash.application.port.out.LlmPort;
import com.prodash.application.port.out.ProposalRepositoryPort;
import com.prodash.config.BatchSizeManager;
import com.prodash.domain.model.Proposal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProposalIngestionService implements IngestProposalsUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProposalIngestionService.class);

    private final CamaraApiPort camaraApiPort;
    private final ProposalRepositoryPort proposalRepositoryPort;
    private final LlmPort llmPort;
    private final BatchSizeManager batchSizeManager;

    public ProposalIngestionService(CamaraApiPort camaraApiPort, ProposalRepositoryPort proposalRepositoryPort,
                                    LlmPort llmPort, BatchSizeManager batchSizeManager) {
        this.camaraApiPort = camaraApiPort;
        this.proposalRepositoryPort = proposalRepositoryPort;
        this.llmPort = llmPort;
        this.batchSizeManager = batchSizeManager;
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

        int currentBatchSize = batchSizeManager.getBatchSize();
        log.info("Found {} new proposals. Summarizing and saving in batches of up to {}.", newProposals.size(), currentBatchSize);

        List<List<Proposal>> batches = Lists.partition(newProposals, currentBatchSize);

        for (int i = 0; i < batches.size(); i++) {
            List<Proposal> batch = batches.get(i);
            log.info("Processing batch {} of {}: {} proposals.", i + 1, batches.size(), batch.size());

            try {
                List<Proposal> summarizedProposals = llmPort.summarizeProposals(batch);
                summarizedProposals.forEach(proposalRepositoryPort::save);
                log.info("Successfully summarized and saved batch {} of {}.", i + 1, batches.size());
                
                // On success, try to increase the batch size for the next run.
                batchSizeManager.increaseBatchSize();

            } catch (HttpClientErrorException e) {
                // The LlmAdapter now handles batch size reduction, so we just log the failure here.
                log.error("Failed to process batch {} of {}. Status: {}, Response: {}", i + 1, batches.size(), e.getStatusCode(), e.getResponseBodyAsString());
            } catch (Exception e) {
                log.error("An unexpected error occurred while processing batch {} of {}: {}", i + 1, batches.size(), e.getMessage());
            }
        }

        log.info("Finished proposal ingestion process.");
    }
}