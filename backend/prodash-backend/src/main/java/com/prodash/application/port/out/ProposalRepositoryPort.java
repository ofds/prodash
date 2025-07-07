package com.prodash.application.port.out;

import com.prodash.domain.model.Proposal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProposalRepositoryPort {

    /**
     * Saves a proposal to the repository.
     *
     * @param proposal The proposal to save.
     * @return The saved proposal.
     */
    Proposal save(Proposal proposal);

    /**
     * Finds a paginated list of all proposals in the repository.
     *
     * @param pageable The pagination information.
     * @return A paginated list of all proposals.
     */
    Page<Proposal> findAll(Pageable pageable);

    /**
     * Finds a proposal by its unique identifier.
     *
     * @param id The ID of the proposal.
     * @return An Optional containing the proposal if found, or empty otherwise.
     */
    Optional<Proposal> findById(String id);

    /**
     * Finds all proposals that do not yet have a summary.
     *
     * @return A list of proposals with a null summary.
     */
    List<Proposal> findBySummaryIsNull();

    /**
     * Finds all proposals that have a summary but do not have an impact score.
     *
     * @return A list of proposals with a summary but a null impact score.
     */
    List<Proposal> findBySummaryIsNotNullAndImpactScoreIsNull();

    /**
     * Retrieves a list of all proposal IDs.
     *
     * @return A list of all proposal IDs.
     */
    List<String> findAllIds();

    /**
     * Searches for proposals based on dynamic filter criteria.
     *
     * @param searchTerm      The term to search for in the ementa (can be null).
     * @param minImpactScore  The minimum impact score to filter by (can be null).
     * @param pageable        The pagination and sorting information.
     * @return A paginated list of proposals matching the criteria.
     */
    Page<Proposal> search(String searchTerm, Double minImpactScore, Pageable pageable);
}