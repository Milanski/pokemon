package com.mbls.challenge.pokemon.collection.domain.exception;

import com.mbls.challenge.pokemon.shared.domain.DomainException;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;

public class PokemonAlreadyInCollectionException extends DomainException {

    public PokemonAlreadyInCollectionException(PokemonId pokemonId) {
        super("pokemon " + pokemonId + " is already in this trainer's collection");
    }
}
