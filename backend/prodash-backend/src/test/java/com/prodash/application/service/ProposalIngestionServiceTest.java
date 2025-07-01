package com.prodash.application.service;

import com.prodash.application.port.out.CamaraApiPort;
import com.prodash.application.port.out.LlmPort;
import com.prodash.application.port.out.ProposalRepositoryPort;
import com.prodash.domain.model.Proposal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProposalIngestionServiceTest {

    @Mock
    private CamaraApiPort camaraApiPort;

    @Mock
    private ProposalRepositoryPort proposalRepositoryPort;

    @Mock
    private LlmPort llmPort;

    @InjectMocks
    private ProposalIngestionService proposalIngestionService;

    @Test
    void ingestProposals_withNewAndExistingProposals_processesOnlyNew() {
        // Arrange
        Proposal newProposal = new Proposal();
        newProposal.setId("1");
        newProposal.setTitle("New Proposal");

        Proposal existingProposal = new Proposal();
        existingProposal.setId("2");
        existingProposal.setTitle("Existing Proposal");

        Proposal summarizedProposal = new Proposal();
        summarizedProposal.setId("1");
        summarizedProposal.setTitle("New Proposal");
        summarizedProposal.setSummary("This is a summary.");

        // Mock API to return both new and existing proposals
        when(camaraApiPort.fetchLatestProposals()).thenReturn(List.of(newProposal, existingProposal));

        // Mock repository to identify which proposals are new vs. existing
        when(proposalRepositoryPort.findById("1")).thenReturn(Optional.empty()); // New
        when(proposalRepositoryPort.findById("2")).thenReturn(Optional.of(existingProposal)); // Existing

        // Mock LLM to summarize the batch of new proposals
        when(llmPort.summarizeProposals(anyList())).thenReturn(List.of(summarizedProposal));

        // Act
        proposalIngestionService.ingestProposals();

        // Assert
        // Verify that only the new proposal was sent for summarization
        verify(llmPort, times(1)).summarizeProposals(List.of(newProposal));
        
        // Verify that only the summarized new proposal was saved
        verify(proposalRepositoryPort, times(1)).save(summarizedProposal);
        
        // Ensure save was not called for the existing proposal
        verify(proposalRepositoryPort, never()).save(existingProposal);
    }

    @Test
    void ingestProposals_withOnlyExistingProposals_doesNothing() {
        // Arrange
        Proposal existingProposal1 = new Proposal();
        existingProposal1.setId("1");
        
        Proposal existingProposal2 = new Proposal();
        existingProposal2.setId("2");
        
        when(camaraApiPort.fetchLatestProposals()).thenReturn(List.of(existingProposal1, existingProposal2));
        when(proposalRepositoryPort.findById(anyString())).thenReturn(Optional.of(new Proposal()));

        // Act
        proposalIngestionService.ingestProposals();

        // Assert
        // Verify that the LLM port and save methods were never called
        verify(llmPort, never()).summarizeProposals(anyList());
        verify(proposalRepositoryPort, never()).save(any(Proposal.class));
    }
}