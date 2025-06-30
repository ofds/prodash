package com.prodash.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object for holding proposal filter parameters.
 * Names match the parameters from the "Dados Abertos" API documentation.
 */
@Data
public class ProposalFilterDTO {

    // --- Primary Identifier Filters ---
    private List<Integer> id;
    private List<Integer> ano;
    private List<String> siglaTipo;
    private String autor;
    private List<String> keywords;

    // --- Date Filters ---
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataApresentacaoInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataApresentacaoFim;

    // --- Other Filters ---
    private List<Integer> idAutor;
    private Boolean tramitacaoSenado;
    private List<Integer> codSituacao;
    private List<Integer> codTema;

    // --- Pagination and Sorting ---
    private Integer pagina;
    private Integer itens = 100; // Default to max page size for efficiency
    private String ordem = "ASC";
    private String ordenarPor = "id";
}
