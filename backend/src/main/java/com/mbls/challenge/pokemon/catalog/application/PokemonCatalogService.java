package com.mbls.challenge.pokemon.catalog.application;

import com.mbls.challenge.pokemon.catalog.domain.exception.PokemonNotFoundException;
import com.mbls.challenge.pokemon.catalog.domain.model.PokemonDetails;
import com.mbls.challenge.pokemon.catalog.domain.model.PokemonSummary;
import com.mbls.challenge.pokemon.catalog.application.port.in.GetPokemonDetailsUseCase;
import com.mbls.challenge.pokemon.catalog.application.port.in.ListPokemonUseCase;
import com.mbls.challenge.pokemon.catalog.domain.port.out.PokemonCatalogPort;
import com.mbls.challenge.pokemon.shared.domain.PageResult;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;

import java.util.List;

public class PokemonCatalogService implements ListPokemonUseCase, GetPokemonDetailsUseCase {

    private final PokemonCatalogPort pokemonCatalogPort;

    public PokemonCatalogService(PokemonCatalogPort pokemonCatalogPort) {
        this.pokemonCatalogPort = pokemonCatalogPort;
    }

    @Override
    public PageResult<PokemonSummary> list(ListPokemonQuery query) {
        List<PokemonSummary> all = pokemonCatalogPort.fetchAllSummaries();

        List<PokemonSummary> filtered = (query.nameFilter() == null || query.nameFilter().isBlank())
                ? all
                : all.stream()
                    .filter(p -> p.name().toLowerCase().contains(query.nameFilter().toLowerCase()))
                    .toList();

        int page = Math.max(query.page(), 0);
        int size = query.size() <= 0 ? 20 : query.size();
        int fromIndex = Math.min(page * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());

        return new PageResult<>(filtered.subList(fromIndex, toIndex), page, size, filtered.size());
    }

    @Override
    public PokemonDetails getById(PokemonId id) {
        return pokemonCatalogPort.fetchDetails(id).orElseThrow(() -> new PokemonNotFoundException(id));
    }
}
