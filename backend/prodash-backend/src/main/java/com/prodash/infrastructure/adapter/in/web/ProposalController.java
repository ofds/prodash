package com.prodash.infrastructure.adapter.in.web;

import com.prodash.application.port.out.ProposalRepositoryPort;
import com.prodash.domain.model.Proposal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proposals")
public class ProposalController {

    private final ProposalRepositoryPort proposalRepositoryPort;

    public ProposalController(ProposalRepositoryPort proposalRepositoryPort) {
        this.proposalRepositoryPort = proposalRepositoryPort;
    }

    /**
     * Handles GET requests to /api/proposals/search with parameters for filtering and pagination.
     *
     * @param searchTerm A string to search in the proposal's ementa (summary).
     * @param pageable   An object that provides pagination information (page number, size).
     * @return A paginated list of Proposal objects that match the criteria.
     */
    @GetMapping("/search")
    public Page<Proposal> searchProposals(
            @RequestParam(required = false) String searchTerm,
            Pageable pageable) {
        if (searchTerm != null && !searchTerm.isEmpty()) {
            // This assumes you will create a 'findBySearchTerm' method in your repository
            return proposalRepositoryPort.findByEmentaContaining(searchTerm, pageable);
        } else {
            // Returns a paginated list if no search term is provided
            return proposalRepositoryPort.findAll(pageable);
        }
    }
}