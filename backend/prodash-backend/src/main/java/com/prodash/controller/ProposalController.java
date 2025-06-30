
package com.prodash.controller;

import com.prodash.dto.ProposalFilterDTO;
import com.prodash.dto.camara.ProposalDTO;
import com.prodash.model.Proposal;
import com.prodash.repository.ProposalRepository;
import com.prodash.scheduler.DataSyncScheduler;
import com.prodash.service.CamaraApiService;
import com.prodash.service.ProposalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.List;

/**
 * REST controller to expose proposal data and control actions.
 */
@RestController
@RequestMapping("/api/proposals")
public class ProposalController {

    private final ProposalRepository proposalRepository;
    private final DataSyncScheduler dataSyncScheduler;
    private final ProposalService proposalService;
    private final CamaraApiService camaraApiService;

    public ProposalController(ProposalRepository proposalRepository,
                              DataSyncScheduler dataSyncScheduler,
                              ProposalService proposalService,
                              CamaraApiService camaraApiService) {
        this.proposalRepository = proposalRepository;
        this.dataSyncScheduler = dataSyncScheduler;
        this.proposalService = proposalService;
        this.camaraApiService = camaraApiService;
    }

    /**
     * Searches for proposals based on a set of filter criteria.
     * This endpoint directly queries the external API.
     * @param filter The filter criteria passed as request parameters.
     * @return A list of proposals matching the filter.
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProposalDTO>> searchProposals(ProposalFilterDTO filter) {
        List<ProposalDTO> results = camaraApiService.fetchProposalsByFilter(filter);
        return ResponseEntity.ok(results);
    }

    /**
     * Endpoint to get all proposals stored in our database.
     * @return A list of all proposals.
     */
    @GetMapping
    public ResponseEntity<List<Proposal>> getAllProposals() {
        List<Proposal> proposals = proposalRepository.findAll();
        return ResponseEntity.ok(proposals);
    }

    /**
     * Manually triggers the daily data synchronization job.
     * @return A confirmation message.
     */
    @PostMapping("/sync")
    public ResponseEntity<String> triggerSync() {
        dataSyncScheduler.syncProposalData();
        return ResponseEntity.ok("Daily synchronization job triggered successfully!");
    }

    /**
     * Manually triggers the backfill job for a specific year.
     * @param year The year to fetch data for.
     * @return A confirmation message.
     */
    @PostMapping("/backfill")
    public ResponseEntity<String> triggerBackfill(@RequestParam("year") int year) {
        new Thread(() -> proposalService.backfillProposalsForYear(year)).start();
        return ResponseEntity.ok("Backfill job for year " + year + " triggered successfully! Check the logs for progress.");
    }

    /**
     * Manually triggers a full backfill of all historical data.
     * @param startYear Optional parameter to specify the first year to fetch. Defaults to 2015.
     * @return A confirmation message.
     */
    @PostMapping("/backfill-all")
    public ResponseEntity<String> triggerFullBackfill(@RequestParam(value = "startYear", defaultValue = "2015") int startYear) {
        new Thread(() -> proposalService.backfillAllProposals(startYear)).start();
        int currentYear = Year.now().getValue();
        String message = String.format("Full backfill job triggered successfully for years %d to %d. This will take a long time. Check the logs for progress.", startYear, currentYear);
        return ResponseEntity.ok(message);
    }
}
