package com.prodash.infrastructure.adapter.in.scheduler;

import com.prodash.application.port.in.FetchProposalsUseCase;
import com.prodash.application.port.in.ScoreProposalsUseCase;
import com.prodash.application.port.in.SummarizeProposalsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ProposalSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProposalSyncScheduler.class);

    private final FetchProposalsUseCase fetchProposalsUseCase;
    private final SummarizeProposalsUseCase summarizeProposalsUseCase;
    private final ScoreProposalsUseCase scoreProposalsUseCase;

    public ProposalSyncScheduler(FetchProposalsUseCase fetchProposalsUseCase,
                                 SummarizeProposalsUseCase summarizeProposalsUseCase,
                                 ScoreProposalsUseCase scoreProposalsUseCase) {
        this.fetchProposalsUseCase = fetchProposalsUseCase;
        this.summarizeProposalsUseCase = summarizeProposalsUseCase;
        this.scoreProposalsUseCase = scoreProposalsUseCase;
    }

    /**
     * Triggers the proposal fetching process.
     * Runs every 12 hours, starting 5 seconds after application startup.
     */
    @Scheduled(initialDelay = 5000, fixedRateString = "${prodash.scheduler.fetching.rate}") // e.g., 43200000 in properties
    public void triggerFetching() {
        log.info("SCHEDULER: Triggering proposal fetching.");
        fetchProposalsUseCase.fetchNewProposals();
    }

    /**
     * Triggers the proposal summarization process.
     * Runs every 12 hours, starting 5 minutes after application startup.
     */
    @Scheduled(initialDelay = 30000, fixedRateString = "${prodash.scheduler.summarizing.rate}") // e.g., 43200000 in properties
    public void triggerSummarization() {
        log.info("SCHEDULER: Triggering proposal summarization.");
        summarizeProposalsUseCase.summarizeUnsummarizedProposals();
    }

    /**
     * Triggers the proposal scoring process.
     * Runs every 12 hours, starting 10 minutes after application startup.
     */
    @Scheduled(initialDelay = 60000, fixedRateString = "${prodash.scheduler.scoring.rate}") // e.g., 43200000 in properties
    public void triggerScoring() {
        log.info("SCHEDULER: Triggering proposal scoring.");
        scoreProposalsUseCase.scoreProposals();
    }
}