package com.prodash.dto.llm;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LlmResult {
    private Long id;
    private String resumo;
    private String categoria;
    private String impacto; // [NEW] The new impact analysis field
}
