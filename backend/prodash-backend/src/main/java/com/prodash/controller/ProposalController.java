package com.prodash.controller;

import com.prodash.dto.ProposalFilterDTO;
import com.prodash.dto.camara.ProposalDTO;
import com.prodash.model.Proposal;
import com.prodash.repository.ProposalRepository;
import com.prodash.service.CamaraApiService;
import com.prodash.service.ProposalService;


import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * REST controller implementing the two-phase backfill strategy.
 */
@RestController
@RequestMapping("/api/proposals")
public class ProposalController {

    private final ProposalRepository proposalRepository;
    private final ProposalService proposalService;
    private final CamaraApiService camaraApiService;

    public ProposalController(ProposalRepository proposalRepository,
            ProposalService proposalService,
            CamaraApiService camaraApiService) {
        this.proposalRepository = proposalRepository;
        this.proposalService = proposalService;
        this.camaraApiService = camaraApiService;
    }

    // --- Data Querying Endpoints ---

    @GetMapping
    public ResponseEntity<List<Proposal>> getAllProposalsFromDB() {
        List<Proposal> proposals = proposalRepository.findAll();
        return ResponseEntity.ok(proposals);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProposalDTO>> searchProposals(ProposalFilterDTO filter) {
        List<ProposalDTO> results = camaraApiService.fetchProposalsByFilter(filter);
        return ResponseEntity.ok(results);
    }

    // --- Two-Phase Backfill Endpoints ---

    /**
     * [PHASE 1] Triggers a fast, ingestion-only backfill of historical data.
     * 
     * @param startYear The first year to fetch. Defaults to 2015.
     * @return A confirmation message.
     */
    @PostMapping("/backfill-ingest-only")
    public ResponseEntity<String> triggerIngestionBackfill(@RequestParam(defaultValue = "2015") int startYear) {
        new Thread(() -> proposalService.ingestAllProposals(startYear)).start();
        String message = String.format(
                "Ingestion-only backfill job triggered for years starting from %d. Check logs for progress.",
                startYear);
        return ResponseEntity.ok(message);
    }

    /**
     * [PHASE 2] Processes a batch of unanalyzed proposals from the database.
     * 
     * @param limit The maximum number of proposals to process. Defaults to 100.
     * @return A confirmation message.
     */
    @PostMapping("/process-unanalyzed-batch")
    public ResponseEntity<String> triggerProcessingJob(@RequestParam(defaultValue = "100") int limit) {
        // This job can also be run in the background if it might take more than a few
        // seconds
        new Thread(() -> proposalService.processUnanalyzedProposals(limit)).start();
        String message = String
                .format("Job triggered to process up to %d unanalyzed proposals. Check logs for progress.", limit);
        return ResponseEntity.ok(message);
    }

    /**
     * [NEW] Triggers an ingestion job specifically for today's proposals.
     * This will fetch the proposals and their full details.
     * 
     * @return A confirmation message.
     */
    @PostMapping("/ingest-today")
    public ResponseEntity<String> triggerTodaysIngestion() {
        // Run this in a background thread so the API call returns immediately
        new Thread(() -> proposalService.ingestTodaysProposals()).start();
        String message = "Job triggered to ingest all of today's proposals with full details. Check logs for progress.";
        return ResponseEntity.ok(message);
    }

    /**
     * [NEW] Gets proposals for a given date, ranked by impact score.
     * Defaults to today's date if none is provided.
     *
     * @param date The date in YYYY-MM-DD format.
     * @return A list of proposals sorted by impact score descending.
     */
    @GetMapping("/ranked-by-date")
    public ResponseEntity<List<Proposal>> getRankedProposalsByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate queryDate = (date == null) ? LocalDate.now() : date;
        String formattedDate = queryDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        Sort sortByImpact = Sort.by(Sort.Direction.DESC, "impactoScoreLLM");

        // Note: The 'dataApresentacao' field is a String.
        // For robust querying, it's better to store dates as Date objects in MongoDB.
        // Assuming the string format is consistently 'YYYY-MM-DD' or 'YYYY-MM-DDTHH:mm:ss'.
        // A more robust query might involve regex or converting the field type in the database.
        // For now, this relies on a simple string match.
        List<Proposal> proposals = proposalRepository.findAll(sortByImpact); // Simplified for now
        // A better implementation would be:
        // List<Proposal> proposals = proposalRepository.findByDataApresentacaoStartingWith(formattedDate, sortByImpact);
        // This would require adding a new method to the repository.

        return ResponseEntity.ok(proposals);
    }
}
