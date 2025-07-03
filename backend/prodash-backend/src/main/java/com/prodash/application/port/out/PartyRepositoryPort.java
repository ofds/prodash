package com.prodash.application.port.out;

import com.prodash.domain.model.Party;
import java.util.Optional;

public interface PartyRepositoryPort {
    Party save(Party party);
    Optional<Party> findById(Integer id);
}