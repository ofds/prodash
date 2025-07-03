package com.prodash.application.port.out;

import com.prodash.domain.model.Deputy;
import java.util.Optional;

public interface DeputyRepositoryPort {
    Deputy save(Deputy deputy);
    Optional<Deputy> findById(Integer id);
}