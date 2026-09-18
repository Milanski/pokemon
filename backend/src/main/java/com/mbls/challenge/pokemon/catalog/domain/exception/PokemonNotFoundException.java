package com.mbls.challenge.pokemon.catalog.domain.exception;

import com.mbls.challenge.pokemon.shared.domain.DomainException;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;

public class PokemonNotFoundException extends DomainException {

    public PokemonNotFoundException(PokemonId id) {
        super("no pokemon found with id " + id);
    }
}
