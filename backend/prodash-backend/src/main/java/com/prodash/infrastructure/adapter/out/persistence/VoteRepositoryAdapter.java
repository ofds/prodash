package com.prodash.infrastructure.adapter.out.persistence;

import com.prodash.application.port.out.VoteRepositoryPort;
import com.prodash.domain.model.Vote;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class VoteRepositoryAdapter implements VoteRepositoryPort {

    private final VoteMongoRepository mongoRepository;
    private final ProposalMapper mapper;

    public VoteRepositoryAdapter(VoteMongoRepository mongoRepository, ProposalMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Vote> saveAll(List<Vote> votes) {
        List<VoteDocument> docs = votes.stream().map(mapper::toDocument).collect(Collectors.toList());
        return mongoRepository.saveAll(docs).stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}