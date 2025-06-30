package com.prodash.scheduler;

import com.prodash.service.JournalService;
import com.prodash.service.ProposalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Handles the full daily workflow: Ingest, Analyze, and create Journal.
 */
@Component
public class DataSyncScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DataSyncScheduler.class);
    private final ProposalService proposalService;
    private final JournalService journalService;

    public DataSyncScheduler(ProposalService proposalService, JournalService journalService) {
        this.proposalService = proposalService;
        this.journalService = journalService;
    }

    /**
     * The main daily job that orchestrates the entire data pipeline.
     * Runs every day at 4 AM.
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void dailyWorkflow() {
        logger.info("--- Starting Daily Workflow ---");

        // Step 1: Ingest any new proposals from the last day.
        proposalService.ingestRecentProposals();

        // Step 2: Process a batch of any unanalyzed proposals (including the new ones).
        // We process up to 200 per day automatically.
        proposalService.processUnanalyzedProposals(200);

        // Step 3: Create the daily journal entry based on what was just processed.
        journalService.createDailyJournal();

        logger.info("--- Daily Workflow Finished ---");
    }
}