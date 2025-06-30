package com.prodash.scheduler;

import com.prodash.dto.camara.ProposalDTO;
import com.prodash.service.CamaraApiService;
import com.prodash.service.ProposalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handles scheduled tasks for the application, such as syncing data.
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
     * For development, you might use a faster rate like fixedRate = 600000 (every 10 minutes).
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void syncProposalData() {
        logger.info("Starting scheduled job: Syncing proposal data...");
        List<ProposalDTO> proposals = camaraApiService.fetchProposals();
        if (!proposals.isEmpty()) {
            proposalService.processAndSaveProposals(proposals);
        }
        logger.info("Finished scheduled job: Syncing proposal data.");
    }
}
