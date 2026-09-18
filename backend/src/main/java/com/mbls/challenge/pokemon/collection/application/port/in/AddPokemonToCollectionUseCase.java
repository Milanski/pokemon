package com.mbls.challenge.pokemon.collection.application.port.in;

import com.mbls.challenge.pokemon.shared.domain.TrainerId;

public interface AddPokemonToCollectionUseCase {

    CollectionEntryView add(AddPokemonCommand command);

    record AddPokemonCommand(TrainerId trainerId, int pokemonId) {
    }
}
