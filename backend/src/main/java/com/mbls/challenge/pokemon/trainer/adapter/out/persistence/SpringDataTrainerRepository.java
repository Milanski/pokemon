package com.mbls.challenge.pokemon.trainer.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataTrainerRepository extends JpaRepository<TrainerJpaEntity, UUID> {

    Optional<TrainerJpaEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
