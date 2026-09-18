package com.mbls.challenge.pokemon.catalog.adapter.out.pokeapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PokeApiListResponse(int count, List<Item> results) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(String name, String url) {

        /** Extracts the numeric id from a resource URL like ".../pokemon/25/". */
        public int extractId() {
            String trimmed = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
            String lastSegment = trimmed.substring(trimmed.lastIndexOf('/') + 1);
            return Integer.parseInt(lastSegment);
        }
    }
}
