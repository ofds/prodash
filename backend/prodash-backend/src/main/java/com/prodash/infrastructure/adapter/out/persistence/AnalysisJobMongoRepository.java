package com.prodash.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisJobMongoRepository extends MongoRepository<AnalysisJobDocument, String> {
}