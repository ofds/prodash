package com.prodash.infrastructure.adapter.out.camara;

import com.prodash.application.port.out.CamaraApiPort;
import com.prodash.domain.model.Proposal;
import com.prodash.infrastructure.adapter.out.camara.dto.CamaraProposalDTO;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CamaraApiAdapter implements CamaraApiPort {

    private final RestTemplate restTemplate;
    private final String camaraApiUrl;
    private final Gson gson = new Gson();

    public CamaraApiAdapter(RestTemplate restTemplate, @Value("${camara.api.url}") String camaraApiUrl) {
        this.restTemplate = restTemplate;
        this.camaraApiUrl = camaraApiUrl;
    }

    @Override
    public List<Proposal> fetchLatestProposals() {
        System.out.println("Fetching latest proposals from: " + camaraApiUrl);
        String jsonResponse = restTemplate.getForObject(camaraApiUrl, String.class);

        // Use Gson to parse the JSON response into our DTO structure
        CamaraProposalDTO.CamaraApiResponse response = gson.fromJson(jsonResponse, CamaraProposalDTO.CamaraApiResponse.class);

        if (response == null || response.dados == null) {
            return List.of(); // Return an empty list if the response is invalid
        }
        
        // Map the DTOs to our domain model
        return Arrays.stream(response.dados)
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Proposal toDomain(CamaraProposalDTO dto) {
        Proposal proposal = new Proposal();
        proposal.setId(dto.getId());
        proposal.setTitle(dto.getTitle()); // Use the helper method for a clean title
        proposal.setSummary(dto.getEmenta()); // The "ementa" is the official summary
        proposal.setFullTextUrl(dto.getUrlInteiroTeor());
        return proposal;
    }
}