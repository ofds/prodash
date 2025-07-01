package com.prodash.service;

import com.prodash.dto.ProposalFilterDTO;
import com.prodash.dto.camara.LinkDTO;
import com.prodash.dto.camara.PaginatedProposalResponse;
import com.prodash.dto.camara.ProposalDTO;
import com.prodash.dto.camara.ProposalDetailWrapperDTO;
import com.prodash.dto.camara.ProposalDetailsDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for interacting with the external Camara dos Deputados
 * API.
 */
@Service
public class CamaraApiService {

    private static final Logger logger = LoggerFactory.getLogger(CamaraApiService.class);
    private final RestTemplate restTemplate;

    @Value("${camara.api.base-url}")
    private String apiBaseUrl;

    public CamaraApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetches proposals based on a flexible set of filter criteria.
     * 
     * @param filter The DTO containing all filter parameters.
     * @return A list of ProposalDTOs matching the filter.
     */
    public List<ProposalDTO> fetchProposalsByFilter(ProposalFilterDTO filter) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiBaseUrl + "/proposicoes");

        // Dynamically add parameters to the URL if they are present in the filter
        // object
        if (filter.getAno() != null && !filter.getAno().isEmpty()) {
            filter.getAno().forEach(y -> builder.queryParam("ano", y));
        }
        if (filter.getSiglaTipo() != null && !filter.getSiglaTipo().isEmpty()) {
            filter.getSiglaTipo().forEach(s -> builder.queryParam("siglaTipo", s));
        }
        if (filter.getAutor() != null && !filter.getAutor().isBlank()) {
            builder.queryParam("autor", filter.getAutor());
        }
        if (filter.getDataApresentacaoInicio() != null) {
            builder.queryParam("dataApresentacaoInicio", filter.getDataApresentacaoInicio());
        }
        if (filter.getDataApresentacaoFim() != null) {
            builder.queryParam("dataApresentacaoFim", filter.getDataApresentacaoFim());
        }
        // Add other filters here as needed...

        // Add sorting and pagination parameters
        builder.queryParam("ordem", filter.getOrdem());
        builder.queryParam("ordenarPor", filter.getOrdenarPor());
        builder.queryParam("itens", filter.getItens());

        return fetchAllPages(builder.toUriString());
    }

    /**
     * Fetches all proposals for a given year. Used for initial database population.
     * 
     * @param year The year to fetch proposals for.
     * @return A list of all ProposalDTOs from that year.
     */
    public List<ProposalDTO> fetchProposalsByYear(int year) {
        String initialUrl = apiBaseUrl + "/proposicoes?ano=" + year + "&ordem=ASC&ordenarPor=id&itens=100";
        return fetchAllPages(initialUrl);
    }

    /**
     * Generic helper method to handle pagination for any starting URL.
     */
    private List<ProposalDTO> fetchAllPages(String initialUrl) {
        List<ProposalDTO> allProposals = new ArrayList<>();
        String nextUrl = initialUrl;

        while (nextUrl != null && !nextUrl.isEmpty()) {
            logger.info("Fetching proposals from URL: {}", nextUrl);
            try {
                PaginatedProposalResponse response = restTemplate.getForObject(nextUrl,
                        PaginatedProposalResponse.class);

                if (response != null && response.getDados() != null) {
                    allProposals.addAll(response.getDados());
                    logger.info("Fetched {} proposals. Total so far: {}.", response.getDados().size(),
                            allProposals.size());

                    nextUrl = response.getLinks().stream()
                            .filter(link -> "next".equals(link.getRel()))
                            .findFirst()
                            .map(LinkDTO::getHref)
                            .orElse(null);
                } else {
                    nextUrl = null;
                }
            } catch (Exception e) {
                logger.error("Failed to fetch proposals from URL: " + nextUrl, e);
                nextUrl = null;
            }
        }
        logger.info("Finished fetching all pages for initial URL. Total found: {}", allProposals.size());
        return allProposals;
    }

    public ProposalDetailsDTO fetchProposalDetails(Long id) {
        String url = apiBaseUrl + "/proposicoes/" + id;
        try {
            ResponseEntity<ProposalDetailWrapperDTO> response = restTemplate.getForEntity(url,
                    ProposalDetailWrapperDTO.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody().getDados();
            }
        } catch (Exception e) {
            logger.error("Failed to fetch details for proposal ID {}: {}", id, e.getMessage());
        }
        return null;
    }
}
