package com.prodash.infrastructure.adapter.out.camara;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.prodash.application.port.out.CamaraApiPort;
import com.prodash.domain.model.*;
import com.prodash.infrastructure.adapter.out.camara.adapter.LocalDateAdapter;
import com.prodash.infrastructure.adapter.out.camara.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class CamaraApiAdapter implements CamaraApiPort {

    private static final Logger log = LoggerFactory.getLogger(CamaraApiAdapter.class);
    private static final DateTimeFormatter API_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final RestTemplate restTemplate;
    private final String baseApiUrl;
    private final Executor taskExecutor;
    private final Gson gson;

    // DTOs para respostas de detalhe que envolvem um único objeto "dados"
    private static class CamaraProposalDetailResponse { CamaraProposalDTO dados; }
    private static class CamaraDeputyResponse { CamaraDeputyDTO dados; }
    private static class CamaraPartyResponse { CamaraPartyDTO dados; }


    public CamaraApiAdapter(RestTemplate restTemplate,
                            @Value("${camara.api.base-url}") String baseApiUrl,
                            Executor taskExecutor) {
        this.restTemplate = restTemplate;
        this.baseApiUrl = baseApiUrl;
        this.taskExecutor = taskExecutor;
        // Configura o Gson para lidar com LocalDate
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
    }

    @Override
    public List<Voting> fetchVotingsForProposal(String proposalId) {
        String url = buildUrl("/proposicoes/{id}/votacoes", proposalId);
        log.debug("Fetching votings for proposal id: {}", proposalId);
        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            CamaraVotingDTO.CamaraVotingResponse response = gson.fromJson(jsonResponse, CamaraVotingDTO.CamaraVotingResponse.class);
            if (response != null && response.dados != null) {
                return response.dados.stream()
                    .map(dto -> toVotingDomain(dto, proposalId))
                    .collect(Collectors.toList());
            }
            return Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch votings for proposal id: {}. Error: {}", proposalId, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Vote> fetchVotesForVoting(String votingId) {
        String url = buildUrl("/votacoes/{id}/votos", votingId);
        log.debug("Fetching votes for voting id: {}", votingId);
        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            CamaraVoteDTO.CamaraVoteResponse response = gson.fromJson(jsonResponse, CamaraVoteDTO.CamaraVoteResponse.class);
            if (response != null && response.dados != null) {
                return response.dados.stream()
                    .map(dto -> toVoteDomain(dto, votingId))
                    .collect(Collectors.toList());
            }
            return Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch votes for voting id: {}. Error: {}", votingId, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<Deputy> fetchDeputyDetails(Integer deputyId) {
        String url = buildUrl("/deputados/{id}", deputyId);
        log.debug("Fetching details for deputy id: {}", deputyId);
        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            CamaraDeputyResponse response = gson.fromJson(jsonResponse, CamaraDeputyResponse.class);
            if (response != null && response.dados != null) {
                // Aqui você precisará de uma forma de obter o `partyId`.
                // Por simplicidade, vamos deixar como 0 por enquanto.
                // A forma correta seria fazer outra chamada ou ter um cache de partidos.
                Integer partyId = 0; // Placeholder
                return Optional.of(toDeputyDomain(response.dados, partyId));
            }
            return Optional.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch details for deputy id: {}. Error: {}", deputyId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<Party> fetchPartyDetails(Integer partyId) {
        String url = buildUrl("/partidos/{id}", partyId);
        log.debug("Fetching details for party id: {}", partyId);
        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            CamaraPartyResponse response = gson.fromJson(jsonResponse, CamaraPartyResponse.class);
            if (response != null && response.dados != null) {
                return Optional.of(toPartyDomain(response.dados));
            }
            return Optional.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch details for party id: {}. Error: {}", partyId, e.getMessage());
            return Optional.empty();
        }
    }

    // --- Métodos Privados e de Mapeamento ---

    private String buildUrl(String path, Object... vars) {
        return UriComponentsBuilder.fromHttpUrl(this.baseApiUrl).path(path).buildAndExpand(vars).toUriString();
    }
    
    // Mapeadores para os novos domínios
    private Voting toVotingDomain(CamaraVotingDTO dto, String proposalId) {
        return new Voting(dto.id, Integer.parseInt(proposalId), dto.data, dto.descricao);
    }

    private Vote toVoteDomain(CamaraVoteDTO dto, String votingId) {
        return new Vote(votingId, dto.deputado_.id, dto.tipoVoto);
    }

    private Deputy toDeputyDomain(CamaraDeputyDTO dto, Integer partyId) {
        return new Deputy(dto.id, dto.nomeCivil, dto.ultimoStatus.nome, dto.ultimoStatus.siglaUf, partyId);
    }

    private Party toPartyDomain(CamaraPartyDTO dto) {
        return new Party(dto.id, dto.sigla, dto.nome);
    }

    @Override
    public List<String> fetchLatestProposalIds() {
        log.info("Starting parallel proposal ID fetch...");

        // 1. Fetch the first page to get pagination details
        String initialUrl = buildUrlForPage(1);
        CamaraProposalDTO.CamaraApiResponse firstResponse = fetchPage(initialUrl);

        if (firstResponse == null) {
            log.error("Failed to fetch the first page of proposal IDs. Aborting.");
            return Collections.emptyList();
        }

        List<CompletableFuture<List<String>>> futures = new ArrayList<>();
        // Add the result from the first page
        futures.add(CompletableFuture.completedFuture(parseProposalIdsFromResponse(firstResponse)));

        // 2. Determine the total number of pages
        int totalPages = getTotalPages(firstResponse).orElse(1);
        log.info("Total pages to fetch for IDs: {}", totalPages);

        // 3. Create parallel tasks for the remaining pages
        if (totalPages > 1) {
            List<CompletableFuture<List<String>>> remainingFutures = IntStream.rangeClosed(2, totalPages)
                    .mapToObj(this::buildUrlForPage)
                    .map(url -> CompletableFuture.supplyAsync(() -> fetchPage(url), taskExecutor)
                            .thenApply(this::parseProposalIdsFromResponse))
                    .collect(Collectors.toList());
            futures.addAll(remainingFutures);
        }

        // 4. Wait for all futures to complete and collect results
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<String> allProposalIds = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        log.info("Finished parallel fetching. Total proposal IDs retrieved: {}", allProposalIds.size());
        return allProposalIds;
    }

    @Override
    public Optional<Proposal> fetchProposalDetails(String id) {
        String url = buildUrlForProposal(id);
        log.debug("Fetching proposal details for id: {}", id);
        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            CamaraProposalDetailResponse response = gson.fromJson(jsonResponse, CamaraProposalDetailResponse.class);

            if (response != null && response.dados != null) {
                Proposal proposal = toDomain(response.dados);
    
                // Buscar autores (lógica existente)
                if (response.dados.getUriAutores() != null && !response.dados.getUriAutores().isEmpty()) {
                    proposal.setAuthors(fetchAuthorsForProposal(response.dados.getUriAutores()));
                }
    
                // NOVA LÓGICA: Buscar temas
                proposal.setThemes(fetchThemesForProposal(proposal.getId()));
    
                return Optional.of(proposal);
            }
            log.warn("No 'dados' field in response for proposal id: {}", id);
            return Optional.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch proposal details for id: {}. Error: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * NEW METHOD: Fetches the authors for a given proposal URI.
     */
    private List<Author> fetchAuthorsForProposal(String authorsUri) {
        log.debug("Fetching authors from: {}", authorsUri);
        try {
            String jsonResponse = restTemplate.getForObject(authorsUri, String.class);
            CamaraAuthorDTO.CamaraAuthorResponse response = gson.fromJson(jsonResponse, CamaraAuthorDTO.CamaraAuthorResponse.class);

            if (response != null && response.dados != null) {
                return response.dados.stream()
                        .map(this::toAuthorDomain)
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch authors from URI: {}. Error: {}", authorsUri, e.getMessage());
            return Collections.emptyList();
        }
    }


    @Override
    public List<Proposal> fetchProposalDetailsInBatch(List<String> ids) {
        log.info("Fetching details for a batch of {} IDs in parallel.", ids.size());

        List<CompletableFuture<Optional<Proposal>>> futures = ids.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> fetchProposalDetails(id), taskExecutor))
                .collect(Collectors.toList());

        // Wait for all the futures to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Collect the results, filtering out any empty Optionals
        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private CamaraProposalDTO.CamaraApiResponse fetchPage(String url) {
        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            return gson.fromJson(jsonResponse, CamaraProposalDTO.CamaraApiResponse.class);
        } catch (RestClientException e) {
            log.error("Failed to fetch proposals from URL: {}. Error: {}", url, e.getMessage());
            return null;
        }
    }

    private List<String> parseProposalIdsFromResponse(CamaraProposalDTO.CamaraApiResponse response) {
        if (response == null || response.dados == null) {
            return Collections.emptyList();
        }
        return response.dados.stream()
                .map(CamaraProposalDTO::getId)
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    @Override
public List<Theme> fetchThemesForProposal(String proposalId) {
    String url = buildUrl("/proposicoes/{id}/temas", proposalId);
    log.debug("Fetching themes for proposal id: {}", proposalId);
    try {
        String jsonResponse = restTemplate.getForObject(url, String.class);
        CamaraThemeDTO.CamaraThemeResponse response = gson.fromJson(jsonResponse, CamaraThemeDTO.CamaraThemeResponse.class);
        if (response != null && response.dados != null) {
            return response.dados.stream()
                .map(dto -> new Theme(dto.cod, dto.nome))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    } catch (RestClientException e) {
        log.error("Failed to fetch themes for proposal id: {}. Error: {}", proposalId, e.getMessage());
        return Collections.emptyList();
    }
}

    private String buildUrlForPage(int pageNumber) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String daysAgo = LocalDate.now().minusDays(1).format(dtf);

        return UriComponentsBuilder.fromHttpUrl(this.baseApiUrl)
                .path("/proposicoes")
                .queryParam("dataInicio", daysAgo)
                .queryParam("ordem", "DESC")
                .queryParam("ordenarPor", "id")
                .queryParam("itens", 100)
                .queryParam("pagina", pageNumber)
                .toUriString();
    }

    private String buildUrlForProposal(String id) {
        return UriComponentsBuilder.fromHttpUrl(this.baseApiUrl)
                .path("/proposicoes/{id}")
                .buildAndExpand(id)
                .toUriString();
    }

    private Optional<Integer> getTotalPages(CamaraProposalDTO.CamaraApiResponse response) {
        return findLink(response, "last")
                .map(url -> UriComponentsBuilder.fromHttpUrl(url).build().getQueryParams().getFirst("pagina"))
                .map(Integer::parseInt);
    }

    private Optional<String> findLink(CamaraProposalDTO.CamaraApiResponse response, String rel) {
        if (response == null || response.links == null) {
            return Optional.empty();
        }
        return response.links.stream()
                .filter(link -> rel.equalsIgnoreCase(link.rel))
                .map(link -> link.href)
                .findFirst();
    }

    private Proposal toDomain(CamaraProposalDTO dto) {
        Proposal proposal = new Proposal();
        proposal.setId(dto.getId());
        proposal.setUri(dto.getUri()); // MAPPED
        proposal.setSiglaTipo(dto.getSiglaTipo());
        proposal.setDescricaoTipo(dto.getDescricaoTipo()); // MAPPED
        proposal.setNumero(dto.getNumero());
        proposal.setAno(dto.getAno());
        proposal.setEmenta(dto.getEmenta());
        proposal.setEmentaDetalhada(dto.getEmentaDetalhada()); // MAPPED
        proposal.setTitle(dto.getTitle());
        proposal.setFullTextUrl(dto.getUrlInteiroTeor());
        proposal.setUriAutores(dto.getUriAutores()); // MAPPED

        if (dto.getDataApresentacao() != null && !dto.getDataApresentacao().isEmpty()) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(dto.getDataApresentacao(), API_DATE_TIME_FORMATTER);
                proposal.setPresentationDate(ldt.toLocalDate());
            } catch (Exception e) {
                log.error("Could not parse presentation date '{}' for proposal ID {}", dto.getDataApresentacao(), dto.getId(), e);
            }
        }

        CamaraProposalDTO.StatusProposicaoDTO statusDto = dto.getStatusProposicao();
        if (statusDto != null) {
            proposal.setStatus(statusDto.descricaoTramitacao);
            proposal.setSituation(statusDto.descricaoSituacao); // MAPPED
            proposal.setDispatch(statusDto.despacho);
            proposal.setProcessingAgency(statusDto.siglaOrgao);
        }

        return proposal;
    }

    
    private Author toAuthorDomain(CamaraAuthorDTO dto) {
        if (dto == null) return null;
        return new Author(dto.getNome(), dto.getTipo());
    }
}