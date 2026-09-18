package com.mbls.challenge.pokemon.catalog.domain.model;

import com.mbls.challenge.pokemon.shared.domain.PokemonId;

import java.util.List;

public record PokemonDetails(
        PokemonId id,
        String name,
        String spriteUrl,
        List<String> types,
        int heightDecimetres,
        int weightHectograms,
        int baseExperience,
        List<PokemonStat> stats
) {
}
