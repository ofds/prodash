package com.prodash.dto.camara;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProposalDetailWrapperDTO {
    private ProposalDetailsDTO dados;
}