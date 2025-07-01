package com.prodash.dto.llm;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LlmResult {
    private Long id;
    private String resumo;
    private String categoria;
    private String impacto;

    // [NEW] Field for the new impact score.
    // The name must match the JSON output from the new prompt.
    @SerializedName("impacto_score")
    private Integer impactoScore;

    // [NEW] Field for the justification.
    @SerializedName("justificativa")
    private String justificativa;
}