package com.mbls.challenge.pokemon.catalog.domain.model;

import com.mbls.challenge.pokemon.shared.domain.PokemonId;

/** Lightweight projection used for the paginated catalog listing. */
public record PokemonSummary(PokemonId id, String name, String spriteUrl) {
}
