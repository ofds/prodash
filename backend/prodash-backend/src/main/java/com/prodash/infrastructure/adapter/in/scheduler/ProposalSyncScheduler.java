package com.prodash.infrastructure.adapter.in.scheduler;

import com.prodash.application.port.in.FetchProposalsUseCase;
import com.prodash.application.port.in.FetchVotingsUseCase; // 1. IMPORTAR
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
    private final FetchVotingsUseCase fetchVotingsUseCase; // 2. INJETAR O NOVO USE CASE

    public ProposalSyncScheduler(FetchProposalsUseCase fetchProposalsUseCase,
                                 SummarizeProposalsUseCase summarizeProposalsUseCase,
                                 ScoreProposalsUseCase scoreProposalsUseCase,
                                 FetchVotingsUseCase fetchVotingsUseCase) { // 3. ATUALIZAR CONSTRUTOR
        this.fetchProposalsUseCase = fetchProposalsUseCase;
        this.summarizeProposalsUseCase = summarizeProposalsUseCase;
        this.scoreProposalsUseCase = scoreProposalsUseCase;
        this.fetchVotingsUseCase = fetchVotingsUseCase;
    }

    /**
     * Triggers the proposal fetching process.
     */
    @Scheduled(initialDelay = 5000, fixedRateString = "${prodash.scheduler.fetching.rate}")
    public void triggerFetching() {
        log.info("SCHEDULER: Triggering proposal fetching.");
        fetchProposalsUseCase.fetchNewProposals();
    }
    
    /**
     * 4. CRIAR NOVO MÉTODO AGENDADO PARA BUSCAR VOTAÇÕES
     * Triggers the voting data fetching process.
     * Runs after the initial proposal fetching.
     */
    @Scheduled(initialDelay = 60000, fixedRateString = "${prodash.scheduler.voting.rate}") // Ex: 1 minuto de delay
    public void triggerVotingFetching() {
        log.info("SCHEDULER: Triggering voting data fetching.");
        fetchVotingsUseCase.fetchNewVotings();
    }


    /**
     * Triggers the proposal summarization process.
     */
    @Scheduled(initialDelay = 300000, fixedRateString = "${prodash.scheduler.summarizing.rate}") // Aumentado para 5 min
    public void triggerSummarization() {
        log.info("SCHEDULER: Triggering proposal summarization.");
        // O nome do método no seu use case pode ser diferente, ajuste se necessário
        summarizeProposalsUseCase.summarizeUnsummarizedProposals();
    }

    /**
     * Triggers the proposal scoring process.
     */
    @Scheduled(initialDelay = 600000, fixedRateString = "${prodash.scheduler.scoring.rate}") // Aumentado para 10 min
    public void triggerScoring() {
        log.info("SCHEDULER: Triggering proposal scoring.");
        // O nome do método no seu use case pode ser diferente, ajuste se necessário
        scoreProposalsUseCase.scoreProposals();
    }
}