package com.prodash.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProposalMongoRepository extends MongoRepository<ProposalDocument, String> {

    /**
     * Finds all proposal documents where the summary field is null.
     * Spring Data MongoDB automatically implements this method based on its name.
     *
     * @return A list of proposal documents with a null summary.
     */
    List<ProposalDocument> findBySummaryIsNull();

    /**
     * Finds all proposal documents where the summary field is not null and the
     * impactScore field is null. Spring Data MongoDB automatically implements this
     * method based on its name.
     *
     * @return A list of proposal documents with a non-null summary and a null impact score.
     */
    List<ProposalDocument> findBySummaryIsNotNullAndImpactScoreIsNull();

    @Query(value = "{}", fields = "{'_id' : 1}")
    List<ProposalDocument> findAllIds();
}