package com.mbls.challenge.pokemon.catalog.adapter.out.pokeapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.pokeapi")
public record PokeApiProperties(String baseUrl, int connectTimeoutMillis, int readTimeoutMillis) {
}
