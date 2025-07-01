package com.prodash.dto.camara;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * This DTO correctly maps the detailed fields inside the "dados" object
 * from the /proposicoes/{id} API endpoint.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProposalDetailsDTO {

    private Long id;
    private String uri;
    private String siglaTipo;
    private Integer codTipo;
    private int numero;
    private int ano;
    private String ementa;
    private String ementaDetalhada;
    private String dataApresentacao;
    private String justificativa;
    private String descricaoTipo;
    private String urlInteiroTeor;
    private String texto;
    private String urnFinal;
    private String uriOrgaoNumerador;
    private String uriAutores;
    private String uriPropPrincipal;
    private String uriPropAnterior;
    private String uriPropPosterior;
    private StatusProposicao statusProposicao;

    /**
     * Nested class to map the 'statusProposicao' object from the API response.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatusProposicao {
        private String dataHora;
        private Integer sequencia;
        private String siglaOrgao;
        private String uriOrgao;
        private String uriUltimoRelator;
        private String regime;
        private String descricaoTramitacao;
        private String codTipoTramitacao;
        private String descricaoSituacao;
        private String codSituacao;
        private String despacho;
        private String url;
        private String ambito;
        private String apreciacao;
    }
}
