package com.prodash.application.service;

import com.google.common.collect.Lists;
import com.prodash.application.port.in.SummarizeProposalsUseCase;
import com.prodash.application.port.out.LlmPort;
import com.prodash.application.port.out.ProposalRepositoryPort;
import com.prodash.config.BatchSizeManager;
import com.prodash.domain.model.Proposal;
import me.tongfei.progressbar.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProposalSummarizingService implements SummarizeProposalsUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProposalSummarizingService.class);

    private final ProposalRepositoryPort proposalRepositoryPort;
    private final LlmPort llmPort;
    private final BatchSizeManager batchSizeManager;

    public ProposalSummarizingService(ProposalRepositoryPort proposalRepositoryPort,
                                      LlmPort llmPort,
                                      BatchSizeManager batchSizeManager) {
        this.proposalRepositoryPort = proposalRepositoryPort;
        this.llmPort = llmPort;
        this.batchSizeManager = batchSizeManager;
    }

    @Override
    public void summarizeUnsummarizedProposals() {
        log.info("Starting proposal summarization process...");
        List<Proposal> unsummarizedProposals = proposalRepositoryPort.findBySummaryIsNull();

        if (unsummarizedProposals.isEmpty()) {
            log.info("No unsummarized proposals to process.");
            return;
        }

        int currentBatchSize = batchSizeManager.getBatchSize();
        log.info("Found {} unsummarized proposals. Summarizing in batches of up to {}.", unsummarizedProposals.size(), currentBatchSize);

        List<List<Proposal>> batches = Lists.partition(unsummarizedProposals, currentBatchSize);

        // Wrap the process in a ProgressBar
        try (ProgressBar pb = new ProgressBar("Summarizing Proposals", unsummarizedProposals.size())) {
            for (int i = 0; i < batches.size(); i++) {
                List<Proposal> batch = batches.get(i);
                // The log for processing each batch is now replaced by the progress bar

                try {
                    List<Proposal> summarizedProposals = llmPort.summarizeProposals(batch);
                    summarizedProposals.forEach(proposalRepositoryPort::save);
                    
                    // Update progress bar by the number of items successfully processed
                    pb.stepBy(batch.size()); 
                    
                    log.debug("Successfully summarized and saved batch {} of {}.", i + 1, batches.size());

                    // On success, try to increase the batch size for the next run.
                    batchSizeManager.increaseBatchSize();

                } catch (Exception e) {
                    // Step the progress bar even on failure to keep the count accurate
                    pb.stepBy(batch.size());
                    pb.pause(); // Pause the progress bar to print the error message cleanly
                    log.error("Failed to process summarization batch {} of {}: {}", i + 1, batches.size(), e.getMessage());
                    pb.resume(); // Resume the progress bar
                }
            }
        } // The progress bar is automatically closed here

        log.info("Finished proposal summarization process.");
    }
}