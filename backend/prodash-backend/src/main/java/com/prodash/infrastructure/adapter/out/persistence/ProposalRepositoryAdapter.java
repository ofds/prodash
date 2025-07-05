package com.prodash.infrastructure.adapter.out.persistence;

import com.prodash.application.port.out.ProposalRepositoryPort;
import com.prodash.domain.model.Proposal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProposalRepositoryAdapter implements ProposalRepositoryPort {

    private final ProposalMongoRepository mongoRepository;
    private final ProposalMapper mapper;

    public ProposalRepositoryAdapter(ProposalMongoRepository mongoRepository, ProposalMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
    }

    @Override
    public Proposal save(Proposal proposal) {
        ProposalDocument document = mapper.toDocument(proposal);
        ProposalDocument savedDocument = mongoRepository.save(document);
        return mapper.toDomain(savedDocument);
    }

    @Override
    public Page<Proposal> findAll(Pageable pageable) {
        // Fetches a paginated list of documents and maps each page item to a domain object.
        Page<ProposalDocument> documentPage = mongoRepository.findAll(pageable);
        return documentPage.map(mapper::toDomain);
    }

    @Override
    public Page<Proposal> findByEmentaContaining(String searchTerm, Pageable pageable) {
        // Fetches a paginated list of documents matching the search term (case-insensitive)
        // and maps the results to domain objects.
        Page<ProposalDocument> documentPage = mongoRepository.findByEmentaContainingIgnoreCase(searchTerm, pageable);
        return documentPage.map(mapper::toDomain);
    }

    @Override
    public Optional<Proposal> findById(String id) {
        return mongoRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Proposal> findBySummaryIsNull() {
        return mongoRepository.findBySummaryIsNull().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Proposal> findBySummaryIsNotNullAndImpactScoreIsNull() {
        return mongoRepository.findBySummaryIsNotNullAndImpactScoreIsNull().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> findAllIds() {
        return mongoRepository.findAllIds().stream()
                .map(ProposalDocument::getId)
                .collect(Collectors.toList());
    }

    /*
     * Note: The old `findAll()` method that returned a List<Proposal> is no longer
     * defined in the port and can be removed, as we are now using pagination.
     */
}