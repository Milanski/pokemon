package com.mbls.challenge.pokemon.catalog.adapter.in.web.dto;

import com.mbls.challenge.pokemon.catalog.domain.model.PokemonSummary;

public record PokemonSummaryResponse(int id, String name, String spriteUrl) {

    public static PokemonSummaryResponse from(PokemonSummary summary) {
        return new PokemonSummaryResponse(summary.id().value(), summary.name(), summary.spriteUrl());
    }
}
