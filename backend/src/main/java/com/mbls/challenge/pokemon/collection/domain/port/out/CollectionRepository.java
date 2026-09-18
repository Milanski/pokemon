package com.mbls.challenge.pokemon.collection.domain.port.out;

import com.mbls.challenge.pokemon.collection.domain.model.Collection;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;
import com.mbls.challenge.pokemon.shared.domain.TrainerId;

import java.util.Optional;

public interface CollectionRepository {

    Optional<Collection> findByTrainerId(TrainerId trainerId);

    /**
     * Whether the trainer already has this Pokémon, without needing to load
     * the whole collection just to find out.
     */
    boolean existsEntry(TrainerId trainerId, PokemonId pokemonId);

    Collection save(Collection collection);
}
