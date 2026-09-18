package com.mbls.challenge.pokemon.collection.domain.model;

import com.mbls.challenge.pokemon.shared.domain.PokemonId;

import java.time.Instant;
import java.util.Objects;

/**
 * Entity within the {@link Collection} aggregate: one captured Pokémon in a
 * trainer's collection. Not an aggregate root itself - it only exists in the
 * context of its owning Collection.
 */
public final class CollectionEntry {

    private final CollectionEntryId id;
    private final PokemonId pokemonId;
    private final String pokemonName;
    private final String spriteUrl;
    private final Instant caughtAt;

    private CollectionEntry(CollectionEntryId id, PokemonId pokemonId, String pokemonName, String spriteUrl,
                             Instant caughtAt) {
        this.id = Objects.requireNonNull(id);
        this.pokemonId = Objects.requireNonNull(pokemonId);
        this.pokemonName = Objects.requireNonNull(pokemonName);
        this.spriteUrl = spriteUrl;
        this.caughtAt = Objects.requireNonNull(caughtAt);
    }

    public static CollectionEntry capture(PokemonId pokemonId, String pokemonName, String spriteUrl) {
        return new CollectionEntry(CollectionEntryId.newId(), pokemonId, pokemonName, spriteUrl, Instant.now());
    }

    public static CollectionEntry reconstitute(CollectionEntryId id, PokemonId pokemonId, String pokemonName,
                                                 String spriteUrl, Instant caughtAt) {
        return new CollectionEntry(id, pokemonId, pokemonName, spriteUrl, caughtAt);
    }

    public CollectionEntryId id() {
        return id;
    }

    public PokemonId pokemonId() {
        return pokemonId;
    }

    public String pokemonName() {
        return pokemonName;
    }

    public String spriteUrl() {
        return spriteUrl;
    }

    public Instant caughtAt() {
        return caughtAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CollectionEntry that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
