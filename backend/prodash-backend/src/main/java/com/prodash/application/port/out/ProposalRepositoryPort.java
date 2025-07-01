package com.prodash.application.port.out;

import com.prodash.domain.model.Proposal;
import java.util.List;
import java.util.Optional;

public interface ProposalRepositoryPort {

    Proposal save(Proposal proposal);

    List<Proposal> findAll();

    Optional<Proposal> findById(String id);

    List<Proposal> findByImpactScoreIsNull();
}