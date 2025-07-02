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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CamaraApiAdapter implements CamaraApiPort {

    private static final Logger log = LoggerFactory.getLogger(CamaraApiAdapter.class);

    private final RestTemplate restTemplate;
    private final String baseApiUrl;
    private final Gson gson = new Gson();

    public CamaraApiAdapter(RestTemplate restTemplate, @Value("${camara.api.url}") String baseApiUrl) {
        this.restTemplate = restTemplate;
        this.baseApiUrl = baseApiUrl;
    }

    @Override
    public List<Proposal> fetchLatestProposals() {
        List<Proposal> allProposals = new ArrayList<>();
        // Build the URL for the first page dynamically
        String nextUrl = buildInitialUrlForToday();

        log.info("Starting proposal fetch. Initial URL: {}", nextUrl);

        // Loop as long as there is a next URL to fetch
        while (nextUrl != null && !nextUrl.isBlank()) {
            try {
                String jsonResponse = restTemplate.getForObject(nextUrl, String.class);
                CamaraProposalDTO.CamaraApiResponse response = gson.fromJson(jsonResponse, CamaraProposalDTO.CamaraApiResponse.class);

                if (response != null && response.dados != null) {
                    List<Proposal> proposalsOnPage = response.dados.stream()
                            .map(this::toDomain)
                            .collect(Collectors.toList());
                    allProposals.addAll(proposalsOnPage);
                    log.info("Fetched {} proposals. Total so far: {}", proposalsOnPage.size(), allProposals.size());
                }

                // Find the URL for the next page from the 'links' field
                nextUrl = findNextPageUrl(response).orElse(null);
                
                if (nextUrl != null) {
                    log.debug("Found next page: {}", nextUrl);
                }

            } catch (RestClientException e) {
                log.error("Failed to fetch proposals from URL: {}. Error: {}", nextUrl, e.getMessage());
                break; // Exit loop on error
            }
        }
        
        log.info("Finished fetching all proposals. Total retrieved: {}", allProposals.size());
        return allProposals;
    }

    /**
     * Constructs the initial URL to fetch all proposals for the current date.
     */
    private String buildInitialUrlForToday() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String today = LocalDate.now().format(dtf);

        return UriComponentsBuilder.fromHttpUrl(this.baseApiUrl)
                .path("/proposicoes")
                .queryParam("dataInicio", today)
                .queryParam("dataFim", today)
                .queryParam("ordem", "DESC")
                .queryParam("ordenarPor", "id")
                .queryParam("itens", 100) // Request max items per page for efficiency
                .toUriString();
    }
    
    /**
     * Finds the URL for the next page from the API response links.
     */
    private Optional<String> findNextPageUrl(CamaraProposalDTO.CamaraApiResponse response) {
        if (response == null || response.links == null) {
            return Optional.empty();
        }
        return response.links.stream()
                .filter(link -> "next".equalsIgnoreCase(link.rel))
                .map(link -> link.href)
                .findFirst();
    }

    private Proposal toDomain(CamaraProposalDTO dto) {
        Proposal proposal = new Proposal();
        proposal.setId(dto.getId());
        proposal.setTitle(dto.getTitle());
        proposal.setSummary(dto.getEmenta());
        proposal.setFullTextUrl(dto.getUrlInteiroTeor());
        return proposal;
    }
}