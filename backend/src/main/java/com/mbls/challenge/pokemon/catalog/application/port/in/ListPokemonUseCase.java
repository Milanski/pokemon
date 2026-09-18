package com.mbls.challenge.pokemon.catalog.application.port.in;

import com.mbls.challenge.pokemon.catalog.domain.model.PokemonSummary;
import com.mbls.challenge.pokemon.shared.domain.PageResult;

public interface ListPokemonUseCase {

    PageResult<PokemonSummary> list(ListPokemonQuery query);

    record ListPokemonQuery(int page, int size, String nameFilter) {
    }
}
