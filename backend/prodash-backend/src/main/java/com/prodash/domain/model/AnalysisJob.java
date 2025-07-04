package com.prodash.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class AnalysisJob {

    private String id;
    private List<String> proposalIds;
    private String userPrompt;
    private Status status;
    private String errorMessage;

    public enum Status {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED
    }

    public AnalysisJob(List<String> proposalIds, String userPrompt) {
        this.proposalIds = proposalIds;
        this.userPrompt = userPrompt;
        this.status = Status.PENDING;
    }
}