package com.prodash.repository;

import com.prodash.model.JournalEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface JournalRepository extends MongoRepository<JournalEntry, String> {

    Optional<JournalEntry> findByDate(LocalDate date);
}
