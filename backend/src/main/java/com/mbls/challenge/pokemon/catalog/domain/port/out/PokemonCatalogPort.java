package com.mbls.challenge.pokemon.catalog.domain.port.out;

import com.mbls.challenge.pokemon.catalog.domain.model.PokemonDetails;
import com.mbls.challenge.pokemon.catalog.domain.model.PokemonSummary;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;

import java.util.List;
import java.util.Optional;

/** Outbound port to the external Pokémon catalog (PokéAPI). */
public interface PokemonCatalogPort {

    /** The full list of known Pokémon (id + name), used as the basis for local pagination/search. */
    List<PokemonSummary> fetchAllSummaries();

    Optional<PokemonDetails> fetchDetails(PokemonId id);
}
