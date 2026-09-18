package com.mbls.challenge.pokemon.trainer.domain.port.out;

import com.mbls.challenge.pokemon.shared.domain.TrainerId;
import com.mbls.challenge.pokemon.trainer.domain.model.Trainer;

import java.util.Optional;

/** Outbound port for persisting and retrieving trainers. */
public interface TrainerRepository {

    Trainer save(Trainer trainer);

    Optional<Trainer> findByUsername(String username);

    Optional<Trainer> findById(TrainerId id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
