package com.prodash.application.port.out;

import com.prodash.domain.model.Proposal;
import java.util.List;
import java.util.Optional;

public interface ProposalRepositoryPort {

    /**
     * Saves a proposal to the repository.
     * @param proposal The proposal to save.
     * @return The saved proposal.
     */
    Proposal save(Proposal proposal);

    /**
     * Finds all proposals in the repository.
     * @return A list of all proposals.
     */
    List<Proposal> findAll();

    /**
     * Finds a proposal by its unique identifier.
     * @param id The ID of the proposal.
     * @return An Optional containing the proposal if found, otherwise empty.
     */
    Optional<Proposal> findById(String id);

    /**
     * Finds all proposals that do not yet have a summary.
     * @return A list of proposals with a null summary.
     */
    List<Proposal> findBySummaryIsNull();

    /**
     * Finds all proposals that have a summary but do not yet have an impact score.
     * @return A list of proposals with a non-null summary and a null impact score.
     */
    List<Proposal> findBySummaryIsNotNullAndImpactScoreIsNull();

    /**
     * Finds all proposal IDs currently in the repository.
     * @return A list of all existing proposal IDs.
     */
    List<String> findAllIds();
}