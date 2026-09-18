package com.mbls.challenge.pokemon.collection.domain.exception;

import com.mbls.challenge.pokemon.shared.domain.DomainException;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;

/** The catalog has no record of this Pokémon id, so it cannot be added to a collection. */
public class UnknownPokemonException extends DomainException {

    public UnknownPokemonException(PokemonId pokemonId) {
        super("no pokemon found with id " + pokemonId);
    }
}
