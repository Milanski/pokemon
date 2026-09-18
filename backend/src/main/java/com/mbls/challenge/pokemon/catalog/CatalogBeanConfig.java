package com.mbls.challenge.pokemon.catalog;

import com.mbls.challenge.pokemon.catalog.domain.port.out.PokemonCatalogPort;
import com.mbls.challenge.pokemon.catalog.application.PokemonCatalogService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class CatalogBeanConfig {

    @Bean
    PokemonCatalogService pokemonCatalogService(PokemonCatalogPort pokemonCatalogPort) {
        return new PokemonCatalogService(pokemonCatalogPort);
    }
}
