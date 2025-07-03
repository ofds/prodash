package com.prodash.application.port.out;

import com.prodash.domain.model.Deputy;
import com.prodash.domain.model.Party;
import com.prodash.domain.model.Proposal;
import com.prodash.domain.model.Vote;
import com.prodash.domain.model.Voting;

import java.util.List;
import java.util.Optional;

public interface CamaraApiPort {

    /**
     * Fetches the IDs of the latest legislative proposals.
     *
     * @return A list of proposal IDs.
     */
    List<String> fetchLatestProposalIds();

    /**
     * Fetches the detailed information for a specific proposal by its ID.
     *
     * @param id The ID of the proposal to fetch.
     * @return An Optional containing the detailed proposal if found, otherwise empty.
     */
    Optional<Proposal> fetchProposalDetails(String id);

    /**
     * Fetches details for a batch of proposal IDs in parallel.
     *
     * @param ids A list of proposal IDs.
     * @return A list of proposals with their details.
     */
    List<Proposal> fetchProposalDetailsInBatch(List<String> ids);

    List<Voting> fetchVotingsForProposal(String proposalId);
    List<Vote> fetchVotesForVoting(String votingId);
    Optional<Deputy> fetchDeputyDetails(Integer deputyId);
    Optional<Party> fetchPartyDetails(Integer partyId);
}