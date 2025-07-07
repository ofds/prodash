package com.prodash.infrastructure.adapter.out.persistence;

import com.prodash.infrastructure.adapter.out.persistence.ProposalDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.ArrayList;
import java.util.List;

public class CustomProposalRepositoryImpl implements CustomProposalRepository {

    private final MongoTemplate mongoTemplate;

    public CustomProposalRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<ProposalDocument> search(String searchTerm, Double minImpactScore, Pageable pageable) {
        Query query = new Query().with(pageable);
        List<Criteria> criteriaList = new ArrayList<>();

        if (searchTerm != null && !searchTerm.isBlank()) {
            // Using regex for a case-insensitive "contains" search
            criteriaList.add(Criteria.where("ementa").regex(searchTerm, "i"));
        }

        if (minImpactScore != null) {
            criteriaList.add(Criteria.where("impactScore").gte(minImpactScore));
        }

        // Add more criteria here as new filters are needed...

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        List<ProposalDocument> proposals = mongoTemplate.find(query, ProposalDocument.class);

        // Execute a separate count query for pagination
        return PageableExecutionUtils.getPage(
                proposals,
                pageable,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), ProposalDocument.class)
        );
    }
}