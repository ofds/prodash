package com.prodash.repository;

import com.prodash.model.Proposal;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data MongoDB repository for the Proposal entity.
 * Provides CRUD operations out of the box.
 */
@Repository
public interface ProposalRepository extends MongoRepository<Proposal, String> {

    /**
     * Custom query to find a proposal by its original ID from the Camara API.
     * This is useful for checking if we have already ingested a proposal.
     *
     * @param originalId The ID from the dadosabertos.camara.leg.br API.
     * @return An Optional containing the Proposal if found.
     */
    Optional<Proposal> findByOriginalId(Long originalId);
}
