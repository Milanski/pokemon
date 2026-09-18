package com.mbls.challenge.pokemon.collection.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataCollectionRepository extends JpaRepository<CollectionJpaEntity, UUID> {

    Optional<CollectionJpaEntity> findByTrainerId(UUID trainerId);
}
