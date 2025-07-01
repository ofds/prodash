package com.example.proposalai.domain.service;

import com.example.proposalai.domain.model.Proposal;

import java.util.Objects;

public class ProposalValidator {

    public void validate(Proposal proposal) {
        Objects.requireNonNull(proposal, "The proposal cannot be null.");
        Objects.requireNonNull(proposal.getId(), "Proposal ID cannot be null.");
        Objects.requireNonNull(proposal.getTitle(), "Proposal title cannot be null.");
        if (proposal.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Proposal title cannot be empty.");
        }
        // Add more validation rules as needed
    }
}