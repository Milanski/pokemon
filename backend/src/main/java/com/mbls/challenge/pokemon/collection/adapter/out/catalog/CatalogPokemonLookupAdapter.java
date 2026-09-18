package com.mbls.challenge.pokemon.collection.adapter.out.catalog;

import com.mbls.challenge.pokemon.catalog.domain.exception.PokemonNotFoundException;
import com.mbls.challenge.pokemon.catalog.application.port.in.GetPokemonDetailsUseCase;
import com.mbls.challenge.pokemon.collection.application.port.out.PokemonCatalogLookupPort;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Bridges the collection context's {@link PokemonCatalogLookupPort} to the
 * catalog context's {@link GetPokemonDetailsUseCase} inbound port. This is
 * the only class in the {@code collection} package tree that is allowed to
 * import anything from {@code catalog} - everywhere else, the two contexts
 * are wired together only here, at the infrastructure edge, not through
 * their domain models.
 */
@Component
class CatalogPokemonLookupAdapter implements PokemonCatalogLookupPort {

    private final GetPokemonDetailsUseCase getPokemonDetailsUseCase;

    CatalogPokemonLookupAdapter(GetPokemonDetailsUseCase getPokemonDetailsUseCase) {
        this.getPokemonDetailsUseCase = getPokemonDetailsUseCase;
    }

    @Override
    public Optional<PokemonSnapshot> lookup(PokemonId pokemonId) {
        try {
            var details = getPokemonDetailsUseCase.getById(pokemonId);
            return Optional.of(new PokemonSnapshot(details.name(), details.spriteUrl()));
        } catch (PokemonNotFoundException e) {
            return Optional.empty();
        }
    }
}
