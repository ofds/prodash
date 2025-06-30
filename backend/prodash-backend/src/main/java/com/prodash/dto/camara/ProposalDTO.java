package com.prodash.dto.camara;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

// Main DTO for a single proposal from the Camara API
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Ignores any fields in the JSON that we don't map
public class ProposalDTO {
    private Long id;
    private String uri;
    private String siglaTipo;
    private int codTipo;
    private int numero;
    private int ano;
    private String ementa; // The summary text of the proposal
}