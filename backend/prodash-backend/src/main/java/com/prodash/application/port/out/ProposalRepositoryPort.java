package com.prodash.application.port.out;

import com.prodash.domain.model.Proposal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProposalRepositoryPort {

    Proposal save(Proposal proposal);

    /**
     * Finds a paginated list of all proposals in the repository.
     * @param pageable The pagination information.
     * @return A paginated list of all proposals.
     */
    Page<Proposal> findAll(Pageable pageable);

    /**
     * Finds proposals whose ementa (summary) contains the given search term, with pagination.
     * @param searchTerm The term to search for within the proposal's ementa.
     * @param pageable   The pagination information.
     * @return A paginated list of matching proposals.
     */
    Page<Proposal> findByEmentaContaining(String searchTerm, Pageable pageable);

    Optional<Proposal> findById(String id);

    List<Proposal> findBySummaryIsNull();

    List<Proposal> findBySummaryIsNotNullAndImpactScoreIsNull();

    List<String> findAllIds();

    // The old findAll() is no longer needed if you always paginate.
    // List<Proposal> findAll();
}