package com.prodash.infrastructure.adapter.in.scheduler;

import com.prodash.application.port.in.FetchProposalsUseCase;
import com.prodash.application.port.in.FetchVotingsUseCase;
import com.prodash.application.port.in.ScoreProposalsUseCase;
import com.prodash.application.port.in.SummarizeProposalsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    /**
     * Roda o processo de sincronização de dados completo de forma sequencial.
     * Este único método agendado garante que os dados sejam buscados e processados na ordem correta.
     */
    @Scheduled(initialDelay = 5000, cron = "${prodash.sync.cron}") // Use uma única propriedade para o ciclo completo
    public void runFullDataSync() {
        log.info("====== INICIANDO SINCRONIZAÇÃO COMPLETA DE DADOS ======");

        // Passo 1: Buscar novas proposições e obter a lista de seus IDs.
        log.info("--- Passo 1: Buscando novas proposições...");
        List<String> newProposalIds = fetchProposalsUseCase.fetchNewProposals();

        // Passo 2: Buscar votações APENAS para as proposições que acabaram de ser adicionadas.
        if (newProposalIds != null && !newProposalIds.isEmpty()) {
            log.info("--- Passo 2: Buscando votações para {} novas proposições...", newProposalIds.size());
            fetchVotingsUseCase.fetchNewVotingsForProposals(newProposalIds);
        } else {
            log.info("--- Passo 2: Nenhuma nova proposição encontrada, pulando a busca por votações.");
        }

        // Passo 3: Enriquecer com LLM (sumarização e pontuação).
        log.info("--- Passo 3: Sumarizando e pontuando propostas não processadas...");
        summarizeProposalsUseCase.summarizeUnsummarizedProposals();
        scoreProposalsUseCase.scoreProposals();

        log.info("====== SINCRONIZAÇÃO COMPLETA DE DADOS FINALIZADA ======");
    }
}