package com.mbls.challenge.pokemon.collection.application.port.in;

import com.mbls.challenge.pokemon.shared.domain.TrainerId;

public interface RemovePokemonFromCollectionUseCase {

    void remove(RemovePokemonCommand command);

    record RemovePokemonCommand(TrainerId trainerId, int pokemonId) {
    }
}
