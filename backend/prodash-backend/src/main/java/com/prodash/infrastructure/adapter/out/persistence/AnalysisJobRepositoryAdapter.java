package com.prodash.infrastructure.adapter.out.persistence;

import com.prodash.application.port.out.AnalysisJobRepositoryPort;
import com.prodash.domain.model.AnalysisJob;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class AnalysisJobRepositoryAdapter implements AnalysisJobRepositoryPort {

    private final AnalysisJobMongoRepository mongoRepository;

    public AnalysisJobRepositoryAdapter(AnalysisJobMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public AnalysisJob save(AnalysisJob analysisJob) {
        AnalysisJobDocument document = toDocument(analysisJob);
        AnalysisJobDocument savedDocument = mongoRepository.save(document);
        return toDomain(savedDocument);
    }

    @Override
    public Optional<AnalysisJob> findById(String id) {
        return mongoRepository.findById(id).map(this::toDomain);
    }

    private AnalysisJobDocument toDocument(AnalysisJob domain) {
        AnalysisJobDocument doc = new AnalysisJobDocument();
        doc.setId(domain.getId());
        doc.setProposalIds(domain.getProposalIds());
        doc.setUserPrompt(domain.getUserPrompt());
        doc.setStatus(domain.getStatus());
        doc.setErrorMessage(domain.getErrorMessage());
        return doc;
    }

    private AnalysisJob toDomain(AnalysisJobDocument document) {
        AnalysisJob domain = new AnalysisJob();
        domain.setId(document.getId());
        domain.setProposalIds(document.getProposalIds());
        domain.setUserPrompt(document.getUserPrompt());
        domain.setStatus(document.getStatus());
        domain.setErrorMessage(document.getErrorMessage());
        return domain;
    }
}