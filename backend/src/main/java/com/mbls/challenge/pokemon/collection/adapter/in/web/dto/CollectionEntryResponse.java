package com.mbls.challenge.pokemon.collection.adapter.in.web.dto;

import com.mbls.challenge.pokemon.collection.application.port.in.CollectionEntryView;

import java.time.Instant;

public record CollectionEntryResponse(int pokemonId, String pokemonName, String spriteUrl, Instant caughtAt) {

    public static CollectionEntryResponse from(CollectionEntryView view) {
        return new CollectionEntryResponse(view.pokemonId(), view.pokemonName(), view.spriteUrl(), view.caughtAt());
    }
}
