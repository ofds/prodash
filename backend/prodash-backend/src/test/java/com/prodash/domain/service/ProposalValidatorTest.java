package com.prodash.domain.service;

import org.junit.jupiter.api.Test;

import com.prodash.domain.model.Proposal;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class ProposalValidatorTest {

    private ProposalValidator validator;

    @BeforeEach
    void setUp() {
        // This method runs before each test, ensuring a fresh validator instance.
        validator = new ProposalValidator();
    }

    @Test
    void validate_nullProposal_throwsException() {
        // Test that a NullPointerException is thrown when the proposal is null.
        assertThrows(NullPointerException.class, () -> {
            validator.validate(null);
        }, "The proposal cannot be null.");
    }

    @Test
    void validate_nullId_throwsException() {
        // Test that a NullPointerException is thrown when the proposal ID is null.
        Proposal proposal = new Proposal();
        proposal.setTitle("Valid Title"); // Set other required fields to isolate the test case

        assertThrows(NullPointerException.class, () -> {
            validator.validate(proposal);
        }, "Proposal ID cannot be null.");
    }

    @Test
    void validate_emptyTitle_throwsException() {
        // Test that an IllegalArgumentException is thrown for an empty title.
        Proposal proposal = new Proposal();
        proposal.setId("123");
        proposal.setTitle("  "); // Title with only whitespace

        assertThrows(IllegalArgumentException.class, () -> {
            validator.validate(proposal);
        }, "Proposal title cannot be empty.");
    }
    
    @Test
    void validate_nullTitle_throwsException() {
        // Test that a NullPointerException is thrown when the proposal title is null.
        Proposal proposal = new Proposal();
        proposal.setId("123");
        proposal.setTitle(null);

        assertThrows(NullPointerException.class, () -> {
            validator.validate(proposal);
        }, "Proposal title cannot be null.");
    }

    @Test
    void validate_validProposal_doesNotThrowException() {
        // Test that a valid proposal passes validation without throwing an exception.
        Proposal proposal = new Proposal();
        proposal.setId("123");
        proposal.setTitle("A Valid Proposal Title");

        // assertDoesNotThrow will fail the test if any exception is thrown.
        assertDoesNotThrow(() -> {
            validator.validate(proposal);
        });
    }
}