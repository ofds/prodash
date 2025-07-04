package com.prodash.application.port.out;

import com.prodash.domain.model.Proposal;
import java.util.List;
import com.prodash.domain.model.AnalysisResult;

public interface LlmPort {
    List<Proposal> summarizeProposals(List<Proposal> proposals);
    List<Proposal> scoreProposals(List<Proposal> proposals);
    /**
     * Analyzes a single proposal using a custom user-defined prompt.
     * @param proposal The proposal to analyze.
     * @param userPrompt The custom prompt provided by the user.
     * @param jobId The ID of the job this analysis belongs to.
     * @return The analysis result.
     */
    AnalysisResult analyzeProposal(Proposal proposal, String userPrompt, String jobId);
}