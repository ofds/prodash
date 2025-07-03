package com.prodash.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteMongoRepository extends MongoRepository<VoteDocument, String> {
    // Podemos adicionar métodos de busca customizados aqui depois, se necessário
}