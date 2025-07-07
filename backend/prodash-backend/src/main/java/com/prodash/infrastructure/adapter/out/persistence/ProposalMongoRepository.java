package com.prodash.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProposalMongoRepository extends MongoRepository<ProposalDocument, String> {

    /**
     * Finds a paginated list of proposal documents whose ementa (summary) contains the given search term, ignoring case.
     * @param searchTerm The term to search for.
     * @param pageable   The pagination information.
     * @return A paginated list of matching proposal documents.
     */
    Page<ProposalDocument> findByEmentaContainingIgnoreCase(String searchTerm, Pageable pageable);

    List<ProposalDocument> findBySummaryIsNull();

    List<ProposalDocument> findBySummaryIsNotNullAndImpactScoreIsNull();

    @Query(value = "{}", fields = "{'_id' : 1}")
    List<ProposalDocument> findAllIds();

    // Query for when both search term and impact score are provided
    Page<ProposalDocument> findByEmentaContainingIgnoreCaseAndImpactScoreGreaterThanEqual(
            String searchTerm, Double minImpactScore, Pageable pageable);

    // Query for when only impact score is provided
    Page<ProposalDocument> findByImpactScoreGreaterThanEqual(Double minImpactScore, Pageable pageable);
}