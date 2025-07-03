package com.prodash.application.port.out;

import com.prodash.domain.model.Voting;
import java.util.List;

public interface VotingRepositoryPort {
    Voting save(Voting voting);
    List<Voting> saveAll(List<Voting> votings);
}