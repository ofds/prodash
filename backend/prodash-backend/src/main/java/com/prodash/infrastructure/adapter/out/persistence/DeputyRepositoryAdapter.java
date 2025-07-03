package com.prodash.infrastructure.adapter.out.persistence;

import com.prodash.application.port.out.DeputyRepositoryPort;
import com.prodash.domain.model.Deputy;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class DeputyRepositoryAdapter implements DeputyRepositoryPort {

    private final DeputyMongoRepository mongoRepository;
    private final ProposalMapper mapper;

    public DeputyRepositoryAdapter(DeputyMongoRepository mongoRepository, ProposalMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
    }

    @Override
    public Deputy save(Deputy deputy) {
        DeputyDocument doc = mapper.toDocument(deputy);
        return mapper.toDomain(mongoRepository.save(doc));
    }

    @Override
    public Optional<Deputy> findById(Integer id) {
        return mongoRepository.findById(id).map(mapper::toDomain);
    }
}