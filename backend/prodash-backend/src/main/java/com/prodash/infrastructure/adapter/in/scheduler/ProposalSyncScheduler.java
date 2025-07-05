package com.prodash.infrastructure.adapter.in.scheduler;

import com.prodash.application.port.in.FetchProposalsUseCase;
import com.prodash.application.port.in.FetchVotingsUseCase;
import com.prodash.application.port.in.ScoreProposalsUseCase;
import com.prodash.application.port.in.SummarizeProposalsUseCase;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
public class ProposalSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProposalSyncScheduler.class);

    private final FetchProposalsUseCase fetchProposalsUseCase;
    private final SummarizeProposalsUseCase summarizeProposalsUseCase;
    private final ScoreProposalsUseCase scoreProposalsUseCase;
    private final FetchVotingsUseCase fetchVotingsUseCase;

    public ProposalSyncScheduler(FetchProposalsUseCase fetchProposalsUseCase,
                                 SummarizeProposalsUseCase summarizeProposalsUseCase,
                                 ScoreProposalsUseCase scoreProposalsUseCase,
                                 FetchVotingsUseCase fetchVotingsUseCase) {
        this.fetchProposalsUseCase = fetchProposalsUseCase;
        this.summarizeProposalsUseCase = summarizeProposalsUseCase;
        this.scoreProposalsUseCase = scoreProposalsUseCase;
        this.fetchVotingsUseCase = fetchVotingsUseCase;
    }

    @PostConstruct
    public void onStartup() {
        log.info("Hello Prodash! Starting full data synchronization on application startup.");
        runFullDataSync();
    }

    /**
     * Roda o processo de sincronização de dados completo de forma sequencial.
     * Este único método agendado garante que os dados sejam buscados e processados na ordem correta.
     */
    @Scheduled( cron = "${prodash.sync.cron}") // Use uma única propriedade para o ciclo completo
    public void runFullDataSync() {
        log.info("Innitializing full data synchronization at: {}", ZonedDateTime.now());

        // Passo 1: Buscar novas proposições e obter a lista de seus IDs.
    
        List<String> newProposalIds = fetchProposalsUseCase.fetchNewProposals();

        // Passo 2: Buscar votações APENAS para as proposições que acabaram de ser adicionadas.
        if (newProposalIds != null && !newProposalIds.isEmpty()) {
            fetchVotingsUseCase.fetchNewVotingsForProposals(newProposalIds);
        }

        summarizeProposalsUseCase.summarizeUnsummarizedProposals();
        scoreProposalsUseCase.scoreProposals();

        log.info("Full data synchronization completed at: {}", ZonedDateTime.now());
        log.info("Data synchronization process finished successfully.");
    }
}