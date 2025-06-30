package com.prodash.scheduler;

import com.prodash.dto.ProposalFilterDTO; // Import the filter DTO
import com.prodash.dto.camara.ProposalDTO;
import com.prodash.service.CamaraApiService;
import com.prodash.service.ProposalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Handles scheduled tasks for the application, such as syncing data.
 * This version is updated to use the filtering service.
 */
@Component
public class DataSyncScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DataSyncScheduler.class);
    private final CamaraApiService camaraApiService;
    private final ProposalService proposalService;

    public DataSyncScheduler(CamaraApiService camaraApiService, ProposalService proposalService) {
        this.camaraApiService = camaraApiService;
        this.proposalService = proposalService;
    }

    /**
     * This method will run automatically at a fixed interval to sync data.
     * The cron expression "0 0 4 * * ?" means it runs every day at 4 AM.
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void syncProposalData() {
        logger.info("Starting scheduled job: Syncing proposal data...");

        // Create a filter to fetch all proposals presented in the last 3 days.
        // This ensures we get all new and recently updated items.
        ProposalFilterDTO recentProposalsFilter = new ProposalFilterDTO();
        recentProposalsFilter.setDataApresentacaoInicio(LocalDate.now().minusDays(3));
        
        // Use the new filter-based method to fetch proposals
        List<ProposalDTO> proposals = camaraApiService.fetchProposalsByFilter(recentProposalsFilter);
        
        if (!proposals.isEmpty()) {
            proposalService.processAndSaveProposals(proposals);
        }
        
        logger.info("Finished scheduled job: Syncing proposal data.");
    }
}