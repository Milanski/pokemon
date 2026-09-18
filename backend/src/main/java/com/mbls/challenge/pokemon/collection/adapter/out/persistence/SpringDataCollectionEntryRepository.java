package com.mbls.challenge.pokemon.collection.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataCollectionEntryRepository extends JpaRepository<CollectionEntryJpaEntity, UUID> {

    /**
     * A targeted existence check so "does this trainer already have this
     * Pokémon?" doesn't require loading the trainer's whole collection graph
     * just to answer a yes/no question.
     */
    boolean existsByCollection_TrainerIdAndPokemonId(UUID trainerId, int pokemonId);
}
