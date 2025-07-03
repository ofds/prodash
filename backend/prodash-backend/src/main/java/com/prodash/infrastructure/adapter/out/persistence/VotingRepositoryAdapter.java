package com.prodash.infrastructure.adapter.out.persistence;

import com.prodash.application.port.out.VotingRepositoryPort;
import com.prodash.domain.model.Voting;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class VotingRepositoryAdapter implements VotingRepositoryPort {

    private final VotingMongoRepository mongoRepository;
    private final ProposalMapper mapper;

    public VotingRepositoryAdapter(VotingMongoRepository mongoRepository, ProposalMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
    }

    @Override
    public Voting save(Voting voting) {
        VotingDocument doc = mapper.toDocument(voting);
        return mapper.toDomain(mongoRepository.save(doc));
    }

    @Override
    public List<Voting> saveAll(List<Voting> votings) {
        List<VotingDocument> docs = votings.stream().map(mapper::toDocument).collect(Collectors.toList());
        return mongoRepository.saveAll(docs).stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}