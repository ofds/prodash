package com.prodash.infrastructure.adapter.out.persistence;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "analysis_results")
public class AnalysisResultDocument {

    @Id
    private String id;
    private String jobId;
    private String proposalId;
    private Double score;
    private String justification;
}