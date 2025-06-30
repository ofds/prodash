package com.prodash.controller;

import com.prodash.model.JournalEntry;
import com.prodash.repository.JournalRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/journal")
public class JournalController {

    private final JournalRepository journalRepository;

    public JournalController(JournalRepository journalRepository) {
        this.journalRepository = journalRepository;
    }

    @GetMapping
    public ResponseEntity<JournalEntry> getJournalByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return journalRepository.findByDate(date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/today")
    public ResponseEntity<JournalEntry> getTodaysJournal() {
        return journalRepository.findByDate(LocalDate.now())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}