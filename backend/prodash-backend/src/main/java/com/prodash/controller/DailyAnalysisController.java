package com.prodash.controller;

import com.prodash.service.DailyAnalysisService;
import com.prodash.service.JournalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/daily")
public class DailyAnalysisController {

    private final DailyAnalysisService dailyAnalysisService;
    private final JournalService journalService;

    public DailyAnalysisController(DailyAnalysisService dailyAnalysisService, JournalService journalService) {
        this.dailyAnalysisService = dailyAnalysisService;
        this.journalService = journalService;
    }

    /**
     * Triggers the full analysis workflow for today's proposals.
     */
    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeTodaysProposals() {
        new Thread(() -> dailyAnalysisService.analyzeTodaysProposals()).start();
        return ResponseEntity.ok("Job triggered to analyze today's proposals. Check logs for progress.");
    }

    /**
     * Triggers the creation of the journal based on proposals analyzed today.
     */
    @PostMapping("/create-journal")
    public ResponseEntity<String> createTodaysJournal() {
        new Thread(() -> journalService.createDailyJournal()).start();
        return ResponseEntity.ok("Job triggered to create today's journal. Check logs for progress.");
    }
}
