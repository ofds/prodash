package com.prodash.application.port.out;

import com.prodash.domain.model.AnalysisJob;
import java.util.Optional;

public interface AnalysisJobRepositoryPort {
    AnalysisJob save(AnalysisJob analysisJob);
    Optional<AnalysisJob> findById(String id);
}