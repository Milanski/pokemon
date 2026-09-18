package com.mbls.challenge.pokemon.shared.domain;

/**
 * Identity of a Pokémon as defined by the PokéAPI (its National Pokédex
 * number). Shared kernel between the catalog and collection bounded
 * contexts.
 */
public record PokemonId(int value) {

    public PokemonId {
        if (value <= 0) {
            throw new InvalidValueException("pokemon id must be positive, got: " + value);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
