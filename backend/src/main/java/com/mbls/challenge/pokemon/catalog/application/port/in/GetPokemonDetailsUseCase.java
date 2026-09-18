package com.mbls.challenge.pokemon.catalog.application.port.in;

import com.mbls.challenge.pokemon.catalog.domain.model.PokemonDetails;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;

public interface GetPokemonDetailsUseCase {

    PokemonDetails getById(PokemonId id);
}
