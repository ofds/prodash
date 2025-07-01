package com.prodash.infrastructure.adapter.in.web;

import com.prodash.application.port.out.ProposalRepositoryPort;
import com.prodash.domain.model.Proposal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/proposals")
public class ProposalController {

    private final ProposalRepositoryPort proposalRepositoryPort;

    public ProposalController(ProposalRepositoryPort proposalRepositoryPort) {
        this.proposalRepositoryPort = proposalRepositoryPort;
    }

    /**
     * Handles GET requests to /api/proposals and returns a list of all
     * legislative proposals stored in the database.
     *
     * @return A list of Proposal objects.
     */
    @GetMapping
    public List<Proposal> listAllProposals() {
        return proposalRepositoryPort.findAll();
    }
}