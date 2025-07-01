package com.prodash.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProposalMongoRepository extends MongoRepository<ProposalDocument, String> {

    /**
     * Custom query to find all proposals where the impactScore field is null.
     * Spring Data MongoDB will automatically implement this method based on its name.
     */
    List<ProposalDocument> findByImpactScoreIsNull();
}