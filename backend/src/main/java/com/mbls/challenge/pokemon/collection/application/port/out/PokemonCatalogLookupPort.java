package com.mbls.challenge.pokemon.collection.application.port.out;

import com.mbls.challenge.pokemon.shared.domain.PokemonId;

import java.util.Optional;

/**
 * What the collection context needs to know about a Pokémon before adding it
 * - nothing more. Deliberately shaped in this context's own terms rather
 * than reusing the {@code catalog} context's domain types, so {@code
 * collection} does not depend on {@code catalog}'s domain model; only the
 * adapter implementing this port knows that catalog data actually comes from
 * PokéAPI via the catalog bounded context.
 */
public interface PokemonCatalogLookupPort {

    Optional<PokemonSnapshot> lookup(PokemonId pokemonId);

    record PokemonSnapshot(String name, String spriteUrl) {
    }
}
