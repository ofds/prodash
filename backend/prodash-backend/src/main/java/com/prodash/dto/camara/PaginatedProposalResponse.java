package com.prodash.dto.camara;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

// DTO for the paginated response structure from the Camara API
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginatedProposalResponse {
    private List<ProposalDTO> dados;
    private List<LinkDTO> links;
}