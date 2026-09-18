package com.mbls.challenge.pokemon.collection.domain.model;

import com.mbls.challenge.pokemon.collection.domain.exception.PokemonAlreadyInCollectionException;
import com.mbls.challenge.pokemon.collection.domain.exception.PokemonNotInCollectionException;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;
import com.mbls.challenge.pokemon.shared.domain.TrainerId;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Aggregate root of the collection bounded context: a trainer's personal
 * Pokémon collection. Owns the invariant that a trainer cannot have the same
 * Pokémon twice.
 */
public final class Collection {

    private final CollectionId id;
    private final TrainerId trainerId;
    private final Set<CollectionEntry> entries;
    /**
     * The version this aggregate was loaded at, used by the persistence
     * adapter to detect a lost update: if another request has saved a change
     * since this instance was loaded, the version at save time will no
     * longer match and the write must be rejected rather than silently
     * overwriting the other request's change.
     */
    private final long version;

    private Collection(CollectionId id, TrainerId trainerId, Set<CollectionEntry> entries, long version) {
        this.id = Objects.requireNonNull(id);
        this.trainerId = Objects.requireNonNull(trainerId);
        this.entries = new LinkedHashSet<>(entries);
        this.version = version;
    }

    public static Collection createEmpty(TrainerId trainerId) {
        return new Collection(CollectionId.newId(), trainerId, Set.of(), 0);
    }

    public static Collection reconstitute(CollectionId id, TrainerId trainerId, Set<CollectionEntry> entries,
                                          long version) {
        return new Collection(id, trainerId, entries, version);
    }

    /** Adds a Pokémon to this collection. Throws if it is already present. */
    public CollectionEntry addPokemon(PokemonId pokemonId, String pokemonName, String spriteUrl) {
        if (contains(pokemonId)) {
            throw new PokemonAlreadyInCollectionException(pokemonId);
        }
        CollectionEntry entry = CollectionEntry.capture(pokemonId, pokemonName, spriteUrl);
        entries.add(entry);
        return entry;
    }

    /** Removes a Pokémon from this collection. Throws if it isn't present. */
    public void removePokemon(PokemonId pokemonId) {
        boolean removed = entries.removeIf(entry -> entry.pokemonId().equals(pokemonId));
        if (!removed) {
            throw new PokemonNotInCollectionException(pokemonId);
        }
    }

    public boolean contains(PokemonId pokemonId) {
        return entries.stream().anyMatch(entry -> entry.pokemonId().equals(pokemonId));
    }

    public CollectionId id() {
        return id;
    }

    public TrainerId trainerId() {
        return trainerId;
    }

    public Set<CollectionEntry> entries() {
        return Set.copyOf(entries);
    }

    public long version() {
        return version;
    }
}
