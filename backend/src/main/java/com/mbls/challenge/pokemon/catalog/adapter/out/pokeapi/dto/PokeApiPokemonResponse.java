package com.mbls.challenge.pokemon.catalog.adapter.out.pokeapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PokeApiPokemonResponse(
        int id,
        String name,
        int height,
        int weight,
        @JsonProperty("base_experience") int baseExperience,
        Sprites sprites,
        List<TypeSlot> types,
        List<StatSlot> stats
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Sprites(@JsonProperty("front_default") String frontDefault, Other other) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Other(@JsonProperty("official-artwork") OfficialArtwork officialArtwork) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OfficialArtwork(@JsonProperty("front_default") String frontDefault) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TypeSlot(NamedResource type) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatSlot(@JsonProperty("base_stat") int baseStat, NamedResource stat) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NamedResource(String name, String url) {
    }
}
