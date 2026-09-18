package com.mbls.challenge.pokemon.collection;

import com.mbls.challenge.pokemon.collection.application.CollectionService;
import com.mbls.challenge.pokemon.collection.domain.port.out.CollectionRepository;
import com.mbls.challenge.pokemon.collection.application.port.out.PokemonCatalogLookupPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class CollectionBeanConfig {

    @Bean
    CollectionService collectionService(CollectionRepository collectionRepository,
                                         PokemonCatalogLookupPort pokemonCatalogLookupPort) {
        return new CollectionService(collectionRepository, pokemonCatalogLookupPort);
    }
}
