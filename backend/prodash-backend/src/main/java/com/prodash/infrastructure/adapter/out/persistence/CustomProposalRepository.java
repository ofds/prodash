package com.prodash.infrastructure.adapter.out.persistence;

import com.prodash.infrastructure.adapter.out.persistence.ProposalDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomProposalRepository {
    /**
     * Searches for proposals based on a search term and a minimum impact score.
     *
     * @param searchTerm      The term to search for in the proposal's ementa (summary).
     * @param minImpactScore  The minimum impact score for the proposals.
     * @param pageable        Pagination information.
     * @return A paginated list of proposals matching the criteria.
     */
    Page<ProposalDocument> search(String searchTerm, Double minImpactScore, Pageable pageable);
}