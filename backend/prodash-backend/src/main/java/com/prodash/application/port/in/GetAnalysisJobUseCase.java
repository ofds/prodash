package com.prodash.application.port.in;

import com.prodash.domain.model.AnalysisJob;
import com.prodash.domain.model.AnalysisResult;

import java.util.List;
import java.util.Optional;

public interface GetAnalysisJobUseCase {
    Optional<AnalysisJob> getJobStatus(String jobId);
    List<AnalysisResult> getJobResults(String jobId);
}