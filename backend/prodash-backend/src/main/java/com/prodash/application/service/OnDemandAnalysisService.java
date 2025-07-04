package com.prodash.application.service;

import com.prodash.application.port.in.GetAnalysisJobUseCase;
import com.prodash.application.port.in.StartAnalysisUseCase;
import com.prodash.application.port.out.AnalysisJobRepositoryPort;
import com.prodash.application.port.out.AnalysisResultRepositoryPort;
import com.prodash.application.port.out.LlmPort;
import com.prodash.application.port.out.ProposalRepositoryPort;
import com.prodash.domain.model.AnalysisJob;
import com.prodash.domain.model.AnalysisResult;
import com.prodash.domain.model.Proposal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OnDemandAnalysisService implements StartAnalysisUseCase, GetAnalysisJobUseCase {

    private static final Logger log = LoggerFactory.getLogger(OnDemandAnalysisService.class);

    private final AnalysisJobRepositoryPort analysisJobRepository;
    private final AnalysisResultRepositoryPort analysisResultRepository;
    private final ProposalRepositoryPort proposalRepository;
    private final LlmPort llmPort;

    public OnDemandAnalysisService(
            AnalysisJobRepositoryPort analysisJobRepository,
            AnalysisResultRepositoryPort analysisResultRepository,
            ProposalRepositoryPort proposalRepository,
            LlmPort llmPort) {
        this.analysisJobRepository = analysisJobRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.proposalRepository = proposalRepository;
        this.llmPort = llmPort;
    }

    @Override
    public AnalysisJob startAnalysis(List<String> proposalIds, String userPrompt) {
        log.info("Creating a new analysis job for {} proposals.", proposalIds.size());
        AnalysisJob job = new AnalysisJob(proposalIds, userPrompt);
        AnalysisJob savedJob = analysisJobRepository.save(job);
        runAnalysisJob(savedJob.getId()); // Fire-and-forget async method
        return savedJob;
    }

    @Async
    public void runAnalysisJob(String jobId) {
        log.info("Starting background analysis for job ID: {}", jobId);
        AnalysisJob job = analysisJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalStateException("AnalysisJob not found for ID: " + jobId));

        job.setStatus(AnalysisJob.Status.RUNNING);
        analysisJobRepository.save(job);

        try {
            for (String proposalId : job.getProposalIds()) {
                Proposal proposal = proposalRepository.findById(proposalId)
                        .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId));

                // Call the new method in LlmPort
                AnalysisResult result = llmPort.analyzeProposal(proposal, job.getUserPrompt(), jobId);
                analysisResultRepository.save(result);
            }

            job.setStatus(AnalysisJob.Status.COMPLETED);
            analysisJobRepository.save(job);
            log.info("Analysis job {} completed successfully.", jobId);

        } catch (Exception e) {
            log.error("Analysis job {} failed.", jobId, e);
            job.setStatus(AnalysisJob.Status.FAILED);
            job.setErrorMessage(e.getMessage());
            analysisJobRepository.save(job);
        }
    }

    @Override
    public Optional<AnalysisJob> getJobStatus(String jobId) {
        return analysisJobRepository.findById(jobId);
    }

    @Override
    public List<AnalysisResult> getJobResults(String jobId) {
        return analysisResultRepository.findByJobId(jobId);
    }
}