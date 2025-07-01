package com.prodash.application.service;

import com.prodash.application.port.out.LlmPort;
import com.prodash.application.port.out.ProposalRepositoryPort;
import com.prodash.domain.model.Proposal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProposalScoringServiceTest {

    @Mock
    private ProposalRepositoryPort proposalRepositoryPort;

    @Mock
    private LlmPort llmPort;

    @InjectMocks
    private ProposalScoringService proposalScoringService;

    @Test
    void scoreProposals_withUnscoredProposals_scoresAndSavesBatch() {
        // Arrange
        Proposal unscored1 = new Proposal();
        unscored1.setId("1");
        unscored1.setImpactScore(null);

        Proposal unscored2 = new Proposal();
        unscored2.setId("2");
        unscored2.setImpactScore(null);

        List<Proposal> unscoredList = List.of(unscored1, unscored2);

        Proposal scored1 = new Proposal();
        scored1.setId("1");
        scored1.setImpactScore(8.5);

        Proposal scored2 = new Proposal();
        scored2.setId("2");
        scored2.setImpactScore(6.0);
        
        List<Proposal> scoredList = List.of(scored1, scored2);

        // Mock repository to return a batch of unscored proposals
        when(proposalRepositoryPort.findByImpactScoreIsNull()).thenReturn(unscoredList);

        // Mock LLM to return the batch of scored proposals
        when(llmPort.scoreProposals(anyList())).thenReturn(scoredList);

        // Act
        proposalScoringService.scoreProposals();

        // Assert
        // Verify the entire list was sent to the LLM
        verify(llmPort, times(1)).scoreProposals(unscoredList);
        
        // Verify that save was called for each of the scored proposals
        verify(proposalRepositoryPort, times(1)).save(scored1);
        verify(proposalRepositoryPort, times(1)).save(scored2);
    }

    @Test
    void scoreProposals_withNoUnscoredProposals_doesNothing() {
        // Arrange
        when(proposalRepositoryPort.findByImpactScoreIsNull()).thenReturn(List.of());

        // Act
        proposalScoringService.scoreProposals();

        // Assert
        // Verify no interactions with the LLM or saving
        verify(llmPort, never()).scoreProposals(anyList());
        verify(proposalRepositoryPort, never()).save(any(Proposal.class));
    }
}