package com.prodash.infrastructure.adapter.out.persistence;

import com.prodash.application.port.out.PartyRepositoryPort;
import com.prodash.domain.model.Party;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class PartyRepositoryAdapter implements PartyRepositoryPort {

    private final PartyMongoRepository mongoRepository;
    private final ProposalMapper mapper;

    public PartyRepositoryAdapter(PartyMongoRepository mongoRepository, ProposalMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
    }

    @Override
    public Party save(Party party) {
        PartyDocument doc = mapper.toDocument(party);
        return mapper.toDomain(mongoRepository.save(doc));
    }

    @Override
    public Optional<Party> findById(Integer id) {
        return mongoRepository.findById(id).map(mapper::toDomain);
    }
}