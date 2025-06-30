package com.prodash.controller;

import com.prodash.model.JournalEntry;
import com.prodash.repository.JournalRepository;
import com.prodash.service.JournalService; // Import JournalService
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/journal")
public class JournalController {

    private final JournalRepository journalRepository;
    private final JournalService journalService; // Inject JournalService

    public JournalController(JournalRepository journalRepository, JournalService journalService) { // Update constructor
        this.journalRepository = journalRepository;
        this.journalService = journalService;
    }

    // --- GET Endpoints ---

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

    // --- POST Endpoint for On-Demand Creation ---

    /**
     * [NEW] Manually triggers the creation of the journal for the current day.
     * This is useful for testing or generating the journal immediately.
     * @return A confirmation message.
     */
    @PostMapping("/create-today")
    public ResponseEntity<String> createTodaysJournalOnDemand() {
        // Running this in a background thread is good practice for potentially long operations
        new Thread(() -> journalService.createDailyJournal()).start();
        return ResponseEntity.ok("Job to create today's journal has been triggered. Check logs for progress.");
    }
}