package com.prodash.application.port.out;

import com.prodash.domain.model.Proposal;
import java.util.List;

public interface CamaraApiPort {

    /**
     * Fetches the latest legislative proposals.
     * @return A list of proposals.
     */
    List<Proposal> fetchLatestProposals();
}