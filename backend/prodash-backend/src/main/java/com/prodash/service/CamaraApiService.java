package com.prodash.service;

import com.prodash.dto.camara.PaginatedProposalResponse;
import com.prodash.dto.camara.ProposalDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;


/**
 * Service responsible for interacting with the external Camara dos Deputados API.
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
     * Fetches a list of proposals from the API.
     * @return A list of ProposalDTOs.
     */
    public List<ProposalDTO> fetchProposals() {
        // For this example, we'll fetch the first page of recent proposals.
        // A full implementation would handle pagination to get all results.
        String url = apiBaseUrl + "/proposicoes?ordem=DESC&ordenarPor=id";
        try {
            PaginatedProposalResponse response = restTemplate.getForObject(url, PaginatedProposalResponse.class);
            if (response != null && response.getDados() != null) {
                logger.info("Successfully fetched {} proposals from the API.", response.getDados().size());
                return response.getDados();
            }
        } catch (Exception e) {
            logger.error("Failed to fetch proposals from Camara API", e);
        }
        return Collections.emptyList();
    }
}
