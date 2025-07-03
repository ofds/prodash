package com.prodash.application.service;

import com.google.common.collect.Lists;
import com.prodash.application.port.in.FetchProposalsUseCase;
import com.prodash.application.port.out.CamaraApiPort;
import com.prodash.application.port.out.ProposalRepositoryPort;
import com.prodash.domain.model.Proposal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProposalFetchingService implements FetchProposalsUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProposalFetchingService.class);
    private static final int BATCH_SIZE = 10;

    private final CamaraApiPort camaraApiPort;
    private final ProposalRepositoryPort proposalRepositoryPort;

    public ProposalFetchingService(CamaraApiPort camaraApiPort, ProposalRepositoryPort proposalRepositoryPort) {
        this.camaraApiPort = camaraApiPort;
        this.proposalRepositoryPort = proposalRepositoryPort;
    }

    @Override
    public void fetchNewProposals() {
        log.info("Starting new proposal fetching process...");
        List<String> allProposalIds = camaraApiPort.fetchLatestProposalIds();

        if (allProposalIds.isEmpty()) {
            log.info("No proposals found from the external API.");
            return;
        }

        log.info("Fetched {} proposal IDs. Filtering for new ones...", allProposalIds.size());

        // 1. Filter for new IDs first
        List<String> newProposalIds = allProposalIds.parallelStream()
                .filter(id -> proposalRepositoryPort.findById(id).isEmpty())
                .collect(Collectors.toList());

        if (newProposalIds.isEmpty()) {
            log.info("No new proposals to fetch and save.");
            log.info("Finished proposal fetching process.");
            return;
        }

        log.info("Found {} new proposals. Fetching details in parallel batches of {}.", newProposalIds.size(), BATCH_SIZE);

        // 2. Partition the new IDs into batches
        List<List<String>> batches = Lists.partition(newProposalIds, BATCH_SIZE);
        
        // 3. Process each batch
        int savedCount = 0;
        for (int i = 0; i < batches.size(); i++) {
            List<String> batch = batches.get(i);
            log.info("Processing batch {} of {}...", i + 1, batches.size());
            
            List<Proposal> fetchedProposals = camaraApiPort.fetchProposalDetailsInBatch(batch);
            
            for (Proposal p : fetchedProposals) {
                proposalRepositoryPort.save(p);
            }
            savedCount += fetchedProposals.size();
        }

        log.info("Successfully fetched and saved {} new proposals.", savedCount);
        log.info("Finished proposal fetching process.");
    }
}