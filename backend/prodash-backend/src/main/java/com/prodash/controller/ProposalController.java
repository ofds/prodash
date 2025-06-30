package com.prodash.controller;

import com.prodash.model.Proposal;
import com.prodash.repository.ProposalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.prodash.scheduler.DataSyncScheduler;

import java.util.List;

/**
 * REST controller to expose proposal data to the frontend.
 */
@RestController
@RequestMapping("/api/proposals")
public class ProposalController {

    private final ProposalRepository proposalRepository;
    private final DataSyncScheduler dataSyncScheduler; // Inject the scheduler

    public ProposalController(ProposalRepository proposalRepository, DataSyncScheduler dataSyncScheduler) {
        this.proposalRepository = proposalRepository;
        this.dataSyncScheduler = dataSyncScheduler;
    }

    /**
     * Endpoint to get all proposals stored in our database.
     * @return A list of all proposals.
     */
    @GetMapping
    public ResponseEntity<List<Proposal>> getAllProposals() {
        List<Proposal> proposals = proposalRepository.findAll();
        return ResponseEntity.ok(proposals);
    }

    /**
     * [FOR TESTING ONLY]
     * Manually triggers the data synchronization job.
     * @return A confirmation message.
     */
    @PostMapping("/sync") // Using POST is better for triggering an action
    public ResponseEntity<String> triggerSync() {
        dataSyncScheduler.syncProposalData();
        return ResponseEntity.ok("Synchronization job triggered successfully!");
    }
}
