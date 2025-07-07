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
        Page<ProposalDocument> documentPage = mongoRepository.findAll(pageable);
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

    /**
     * Searches for proposals using dynamic criteria.
     * This method delegates the call to the custom repository implementation.
     *
     * @param searchTerm      The term to search for in the ementa. Can be null.
     * @param minImpactScore  The minimum impact score. Can be null.
     * @param pageable        Pagination and sorting information.
     * @return A paginated list of proposals matching the criteria.
     */
    @Override
    public Page<Proposal> search(String searchTerm, Double minImpactScore, Pageable pageable) {
        // The complex if/else logic is now gone.
        // We delegate directly to the repository, which will use our custom implementation.
        Page<ProposalDocument> results = mongoRepository.search(searchTerm, minImpactScore, pageable);
        return results.map(mapper::toDomain);
    }
}