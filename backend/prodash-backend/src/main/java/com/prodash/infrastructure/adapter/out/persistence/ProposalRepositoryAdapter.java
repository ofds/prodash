package com.prodash.infrastructure.adapter.out.persistence;


import com.prodash.application.port.out.ProposalRepositoryPort;
import com.prodash.domain.model.Proposal;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProposalRepositoryAdapter implements ProposalRepositoryPort {

    private final ProposalMongoRepository mongoRepository;
    private final ProposalMapper mapper;

    public ProposalRepositoryAdapter(ProposalMongoRepository mongoRepository, ProposalMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
    }

    @Override
    public Proposal save(Proposal proposal) {
        ProposalDocument document = mapper.toDocument(proposal);
        ProposalDocument savedDocument = mongoRepository.save(document);
        return mapper.toDomain(savedDocument);
    }

    @Override
    public List<Proposal> findAll() {
        return mongoRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Proposal> findById(String id) {
        return mongoRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Proposal> findByImpactScoreIsNull() {
        return mongoRepository.findByImpactScoreIsNull().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}