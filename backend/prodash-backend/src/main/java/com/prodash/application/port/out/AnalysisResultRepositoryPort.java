package com.prodash.application.port.out;

import com.prodash.domain.model.AnalysisResult;
import java.util.List;

public interface AnalysisResultRepositoryPort {
    AnalysisResult save(AnalysisResult analysisResult);
    List<AnalysisResult> findByJobId(String jobId);
}