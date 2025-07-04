package com.prodash.application.port.in;

import com.prodash.domain.model.AnalysisJob;
import java.util.List;

public interface StartAnalysisUseCase {
    AnalysisJob startAnalysis(List<String> proposalIds, String userPrompt);
}