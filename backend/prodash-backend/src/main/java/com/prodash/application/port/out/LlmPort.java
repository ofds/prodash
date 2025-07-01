package com.prodash.application.port.out;

import com.prodash.domain.model.Proposal;
import java.util.List;

public interface LlmPort {
    List<Proposal> summarizeProposals(List<Proposal> proposals);
    List<Proposal> scoreProposals(List<Proposal> proposals);
}