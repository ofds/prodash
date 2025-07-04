package com.prodash.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnalysisResultMongoRepository extends MongoRepository<AnalysisResultDocument, String> {
    List<AnalysisResultDocument> findByJobId(String jobId);
}