package com.mbls.challenge.pokemon.collection.domain.exception;

import com.mbls.challenge.pokemon.shared.domain.DomainException;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;

public class PokemonNotInCollectionException extends DomainException {

    public PokemonNotInCollectionException(PokemonId pokemonId) {
        super("pokemon " + pokemonId + " is not in this trainer's collection");
    }
}
