package com.prodash.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {

    private String id;
    private String jobId;
    private String proposalId;
    private Integer score;
    private String justification;
}