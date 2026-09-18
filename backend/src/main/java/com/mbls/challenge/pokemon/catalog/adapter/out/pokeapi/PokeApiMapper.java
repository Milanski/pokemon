package com.mbls.challenge.pokemon.catalog.adapter.out.pokeapi;

import com.mbls.challenge.pokemon.catalog.adapter.out.pokeapi.dto.PokeApiListResponse;
import com.mbls.challenge.pokemon.catalog.adapter.out.pokeapi.dto.PokeApiPokemonResponse;
import com.mbls.challenge.pokemon.catalog.domain.model.PokemonDetails;
import com.mbls.challenge.pokemon.catalog.domain.model.PokemonStat;
import com.mbls.challenge.pokemon.catalog.domain.model.PokemonSummary;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;
import org.springframework.stereotype.Component;

@Component
class PokeApiMapper {

    private static final String ARTWORK_URL_TEMPLATE =
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/%d.png";

    /** Builds the sprite URL from the id alone, avoiding one API call per list item. */
    String spriteUrlFor(int id) {
        return ARTWORK_URL_TEMPLATE.formatted(id);
    }

    PokemonSummary toSummary(PokeApiListResponse.Item item) {
        int id = item.extractId();
        return new PokemonSummary(new PokemonId(id), item.name(), spriteUrlFor(id));
    }

    PokemonDetails toDetails(PokeApiPokemonResponse response) {
        String spriteUrl = resolveBestSprite(response);

        var types = response.types().stream()
                .map(typeSlot -> typeSlot.type().name())
                .toList();

        var stats = response.stats().stream()
                .map(statSlot -> new PokemonStat(statSlot.stat().name(), statSlot.baseStat()))
                .toList();

        return new PokemonDetails(
                new PokemonId(response.id()),
                response.name(),
                spriteUrl,
                types,
                response.height(),
                response.weight(),
                response.baseExperience(),
                stats
        );
    }

    private String resolveBestSprite(PokeApiPokemonResponse response) {
        if (response.sprites() == null) {
            return spriteUrlFor(response.id());
        }
        if (response.sprites().other() != null
                && response.sprites().other().officialArtwork() != null
                && response.sprites().other().officialArtwork().frontDefault() != null) {
            return response.sprites().other().officialArtwork().frontDefault();
        }
        if (response.sprites().frontDefault() != null) {
            return response.sprites().frontDefault();
        }
        return spriteUrlFor(response.id());
    }
}
