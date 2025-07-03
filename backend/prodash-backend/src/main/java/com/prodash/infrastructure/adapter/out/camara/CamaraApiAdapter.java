package com.prodash.infrastructure.adapter.out.camara;

import com.google.gson.Gson;
import com.prodash.application.port.out.CamaraApiPort;
import com.prodash.domain.model.Proposal;
import com.prodash.infrastructure.adapter.out.camara.dto.CamaraProposalDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
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

    private final RestTemplate restTemplate;
    private final String baseApiUrl;
    private final Executor taskExecutor;
    private final Gson gson = new Gson();

    // DTO for the detail response, which wraps a single proposal DTO
    private static class CamaraProposalDetailResponse {
        CamaraProposalDTO dados;
    }

    public CamaraApiAdapter(RestTemplate restTemplate,
                            @Value("${camara.api.base-url}") String baseApiUrl,
                            Executor taskExecutor) {
        this.restTemplate = restTemplate;
        this.baseApiUrl = baseApiUrl;
        this.taskExecutor = taskExecutor;
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
        log.info("Fetching proposal details for id: {}", id);
        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            CamaraProposalDetailResponse response = gson.fromJson(jsonResponse, CamaraProposalDetailResponse.class);

            if (response != null && response.dados != null) {
                return Optional.of(toDomain(response.dados));
            }
            log.warn("No 'dados' field in response for proposal id: {}", id);
            return Optional.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch proposal details for id: {}. Error: {}", id, e.getMessage());
            return Optional.empty();
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
        proposal.setSiglaTipo(dto.getSiglaTipo());
        proposal.setNumero(dto.getNumero());
        proposal.setAno(dto.getAno());
        proposal.setEmenta(dto.getEmenta());
        proposal.setTitle(dto.getTitle()); // Using the DTO's helper method
        return proposal;
    }
}