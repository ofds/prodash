package com.prodash.dto.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
// Represents the structure of the JSON content we expect from the LLM
@Data
@NoArgsConstructor
public class LlmResult {
    private Long id;
    private String resumo; // "summary" in Portuguese
    private String categoria; // "category" in Portuguese
}
