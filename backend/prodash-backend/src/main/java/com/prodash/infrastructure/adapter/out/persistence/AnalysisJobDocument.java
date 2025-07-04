package com.prodash.infrastructure.adapter.out.persistence;

import com.prodash.domain.model.AnalysisJob;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "analysis_jobs")
public class AnalysisJobDocument {

    @Id
    private String id;
    private List<String> proposalIds;
    private String userPrompt;
    private AnalysisJob.Status status;
    private String errorMessage;
}