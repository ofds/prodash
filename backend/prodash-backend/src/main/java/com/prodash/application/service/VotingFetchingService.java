package com.prodash.application.service;

import com.prodash.application.port.in.FetchVotingsUseCase;
import com.prodash.application.port.out.*;
import com.prodash.domain.model.*;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VotingFetchingService implements FetchVotingsUseCase {

    private static final Logger log = LoggerFactory.getLogger(VotingFetchingService.class);

    private final CamaraApiPort camaraApiPort;
    private final ProposalRepositoryPort proposalRepository;
    private final VotingRepositoryPort votingRepository;
    private final VoteRepositoryPort voteRepository;
    private final DeputyRepositoryPort deputyRepository;
    private final PartyRepositoryPort partyRepository;

    public VotingFetchingService(CamaraApiPort camaraApiPort,
            ProposalRepositoryPort proposalRepository,
            VotingRepositoryPort votingRepository,
            VoteRepositoryPort voteRepository,
            DeputyRepositoryPort deputyRepository,
            PartyRepositoryPort partyRepository) {
        this.camaraApiPort = camaraApiPort;
        this.proposalRepository = proposalRepository;
        this.votingRepository = votingRepository;
        this.voteRepository = voteRepository;
        this.deputyRepository = deputyRepository;
        this.partyRepository = partyRepository;
    }

    @Override
    public void fetchNewVotingsForProposals(List<String> proposalIds) {
        if (proposalIds == null || proposalIds.isEmpty()) {
            log.info("No new proposals to check for votings.");
            return;
        }

        log.info("Checking for new votings for {} new proposals.", proposalIds.size());

        // Create a single ProgressBar instance
        try (ProgressBar pb = ProgressBar.builder()
                .setTaskName("Checking Votings")
                .setInitialMax(proposalIds.size())
                .setStyle(ProgressBarStyle.ASCII)
                .build()) {
            proposalIds.parallelStream().forEach(proposalId -> {
                try {
                    fetchAndSaveVotingsForProposal(proposalId);
                } finally {
                    pb.step(); // Update the progress bar in a thread-safe manner
                }
            });
        }
        log.info("Finished voting data fetching process.");
    }

    private void fetchAndSaveVotingsForProposal(String proposalId) {
        List<Voting> votings = camaraApiPort.fetchVotingsForProposal(proposalId);
        if (votings.isEmpty()) {
            return; // Nenhuma votação para esta proposta
        }

        log.info("Found {} votings for proposal ID {}", votings.size(), proposalId);
        votingRepository.saveAll(votings);

        // Para cada votação, buscar e salvar os votos individuais.
        for (Voting voting : votings) {
            List<Vote> votes = camaraApiPort.fetchVotesForVoting(voting.id());
            if (!votes.isEmpty()) {
                log.debug("Saving {} votes for voting ID {}", votes.size(), voting.id());
                voteRepository.saveAll(votes);

                // Garantir que os deputados e partidos existam no nosso DB
                ensureDeputiesAndPartiesExist(votes);
            }
        }
    }

    /**
     * Otimização: Verifica quais deputados e partidos dos votos já não existem no
     * banco de dados
     * e busca apenas os novos para evitar chamadas de API desnecessárias.
     */
    private void ensureDeputiesAndPartiesExist(List<Vote> votes) {
        Set<Integer> deputyIds = votes.stream().map(Vote::deputadoId).collect(Collectors.toSet());

        // Para cada deputado, busca os detalhes se ele ainda não existir no nosso
        // banco.
        for (Integer deputyId : deputyIds) {
            deputyRepository.findById(deputyId).orElseGet(() -> {
                camaraApiPort.fetchDeputyDetails(deputyId).ifPresent(deputy -> {
                    deputyRepository.save(deputy);
                    // Garante que o partido do deputado também exista
                    partyRepository.findById(deputy.partidoId())
                            .orElseGet(() -> camaraApiPort.fetchPartyDetails(deputy.partidoId())
                                    .map(partyRepository::save)
                                    .orElse(null));
                });
                return null; // findById precisa de um retorno, mas a ação é o que importa.
            });
        }
    }
}