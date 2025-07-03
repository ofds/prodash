package com.prodash.application.port.out;

import com.prodash.domain.model.Vote;
import java.util.List;

public interface VoteRepositoryPort {
    List<Vote> saveAll(List<Vote> votes);
}