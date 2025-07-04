package com.prodash.infrastructure.adapter.in.web;

import com.prodash.application.port.in.GetAnalysisJobUseCase;
import com.prodash.application.port.in.StartAnalysisUseCase;
import com.prodash.domain.model.AnalysisJob;
import com.prodash.domain.model.AnalysisResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final StartAnalysisUseCase startAnalysisUseCase;
    private final GetAnalysisJobUseCase getAnalysisJobUseCase;

    public AnalysisController(StartAnalysisUseCase startAnalysisUseCase, GetAnalysisJobUseCase getAnalysisJobUseCase) {
        this.startAnalysisUseCase = startAnalysisUseCase;
        this.getAnalysisJobUseCase = getAnalysisJobUseCase;
    }

    @PostMapping("/jobs")
    public ResponseEntity<AnalysisJob> startAnalysisJob(@RequestBody AnalysisRequestPayload payload) {
        AnalysisJob startedJob = startAnalysisUseCase.startAnalysis(payload.getProposalIds(), payload.getUserPrompt());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(startedJob);
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<Map<String, Object>> getAnalysisJob(@PathVariable String jobId) {
        return getAnalysisJobUseCase.getJobStatus(jobId)
                .map(job -> {
                    List<AnalysisResult> results = List.of();
                    if (job.getStatus() == AnalysisJob.Status.COMPLETED) {
                        results = getAnalysisJobUseCase.getJobResults(jobId);
                    }
                    Map<String, Object> response = Map.of(
                            "job", job,
                            "results", results
                    );
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Inner DTO class for the request body
    static class AnalysisRequestPayload {
        private List<String> proposalIds;
        private String userPrompt;

        // Getters and Setters
        public List<String> getProposalIds() {
            return proposalIds;
        }

        public void setProposalIds(List<String> proposalIds) {
            this.proposalIds = proposalIds;
        }

        public String getUserPrompt() {
            return userPrompt;
        }

        public void setUserPrompt(String userPrompt) {
            this.userPrompt = userPrompt;
        }
    }
}