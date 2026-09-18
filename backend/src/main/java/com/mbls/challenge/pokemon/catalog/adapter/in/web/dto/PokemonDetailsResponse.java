package com.mbls.challenge.pokemon.catalog.adapter.in.web.dto;

import com.mbls.challenge.pokemon.catalog.domain.model.PokemonDetails;
import com.mbls.challenge.pokemon.catalog.domain.model.PokemonStat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record PokemonDetailsResponse(
        int id,
        String name,
        String spriteUrl,
        List<String> types,
        int heightDecimetres,
        int weightHectograms,
        int baseExperience,
        Map<String, Integer> stats
) {

    public static PokemonDetailsResponse from(PokemonDetails details) {
        Map<String, Integer> stats = details.stats().stream()
                .collect(Collectors.toMap(PokemonStat::name, PokemonStat::baseValue, (a, b) -> a, java.util.LinkedHashMap::new));

        return new PokemonDetailsResponse(
                details.id().value(),
                details.name(),
                details.spriteUrl(),
                details.types(),
                details.heightDecimetres(),
                details.weightHectograms(),
                details.baseExperience(),
                stats
        );
    }
}
