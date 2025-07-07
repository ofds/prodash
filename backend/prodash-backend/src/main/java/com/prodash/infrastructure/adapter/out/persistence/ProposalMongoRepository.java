package com.prodash.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProposalMongoRepository extends MongoRepository<ProposalDocument, String>, CustomProposalRepository {

    /**
     * Finds proposals where the summary field is null.
     * @return A list of proposal documents without a summary.
     */
    List<ProposalDocument> findBySummaryIsNull();

    /**
     * Finds proposals that have a summary but do not have an impact score.
     * @return A list of proposal documents with a summary but no impact score.
     */
    List<ProposalDocument> findBySummaryIsNotNullAndImpactScoreIsNull();

    /**
     * Retrieves all proposal IDs. This is an efficient way to get all IDs without loading the full documents.
     * @return A list of proposal documents containing only the ID field.
     */
    @Query(value = "{}", fields = "{'_id' : 1}")
    List<ProposalDocument> findAllIds();
}