package com.prodash.infrastructure.adapter.in.scheduler;

import com.prodash.application.port.in.IngestProposalsUseCase;
import com.prodash.application.port.in.ScoreProposalsUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ProposalSyncScheduler {

    private final IngestProposalsUseCase ingestProposalsUseCase;
    private final ScoreProposalsUseCase scoreProposalsUseCase;

    public ProposalSyncScheduler(IngestProposalsUseCase ingestProposalsUseCase, ScoreProposalsUseCase scoreProposalsUseCase) {
        this.ingestProposalsUseCase = ingestProposalsUseCase;
        this.scoreProposalsUseCase = scoreProposalsUseCase;
    }

    /**
     * Triggers the proposal ingestion process.
     * Runs 5 seconds after application startup, then every 12 hours.
     */
    @Scheduled(initialDelay = 5000, fixedRate = 43200000) // 5 seconds, then every 12 hours
    public void triggerIngestion() {
        System.out.println("SCHEDULER: Triggering proposal ingestion.");
        ingestProposalsUseCase.ingestProposals();
    }

    /**
     * Triggers the proposal scoring process.
     * Runs 30 seconds after application startup, then every 30 minutes.
     */
    @Scheduled(initialDelay = 30000, fixedRate = 1800000) // 30 seconds, then every 30 minutes
    public void triggerScoring() {
        System.out.println("SCHEDULER: Triggering proposal scoring.");
        scoreProposalsUseCase.scoreProposals();
    }
}