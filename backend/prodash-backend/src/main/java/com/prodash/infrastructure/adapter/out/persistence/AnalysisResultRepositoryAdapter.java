package com.prodash.infrastructure.adapter.out.persistence;

import com.prodash.application.port.out.AnalysisResultRepositoryPort;
import com.prodash.domain.model.AnalysisResult;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AnalysisResultRepositoryAdapter implements AnalysisResultRepositoryPort {

    private final AnalysisResultMongoRepository mongoRepository;

    public AnalysisResultRepositoryAdapter(AnalysisResultMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public AnalysisResult save(AnalysisResult analysisResult) {
        AnalysisResultDocument document = toDocument(analysisResult);
        AnalysisResultDocument savedDocument = mongoRepository.save(document);
        return toDomain(savedDocument);
    }

    @Override
    public List<AnalysisResult> findByJobId(String jobId) {
        return mongoRepository.findByJobId(jobId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private AnalysisResultDocument toDocument(AnalysisResult domain) {
        AnalysisResultDocument doc = new AnalysisResultDocument();
        doc.setId(domain.getId());
        doc.setJobId(domain.getJobId());
        doc.setProposalId(domain.getProposalId());
        doc.setScore(domain.getScore());
        doc.setJustification(domain.getJustification());
        return doc;
    }

    private AnalysisResult toDomain(AnalysisResultDocument document) {
        return new AnalysisResult(
                document.getId(),
                document.getJobId(),
                document.getProposalId(),
                document.getScore(),
                document.getJustification()
        );
    }
}