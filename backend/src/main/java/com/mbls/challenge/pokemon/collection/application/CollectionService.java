package com.mbls.challenge.pokemon.collection.application;

import com.mbls.challenge.pokemon.collection.domain.exception.PokemonAlreadyInCollectionException;
import com.mbls.challenge.pokemon.collection.domain.exception.PokemonNotInCollectionException;
import com.mbls.challenge.pokemon.collection.domain.exception.UnknownPokemonException;
import com.mbls.challenge.pokemon.collection.domain.model.Collection;
import com.mbls.challenge.pokemon.collection.domain.model.CollectionEntry;
import com.mbls.challenge.pokemon.collection.application.port.in.AddPokemonToCollectionUseCase;
import com.mbls.challenge.pokemon.collection.application.port.in.CollectionEntryView;
import com.mbls.challenge.pokemon.collection.application.port.in.ListMyCollectionUseCase;
import com.mbls.challenge.pokemon.collection.application.port.in.RemovePokemonFromCollectionUseCase;
import com.mbls.challenge.pokemon.collection.domain.port.out.CollectionRepository;
import com.mbls.challenge.pokemon.collection.application.port.out.PokemonCatalogLookupPort;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;
import com.mbls.challenge.pokemon.shared.domain.TrainerId;

import java.util.List;

/**
 * Orchestrates the collection use cases, including the cross-context step of
 * resolving canonical Pokémon data before adding it: this lives here, as an
 * application-layer concern, rather than in the web controller, so it can be
 * unit-tested without HTTP and reused by any future entry point.
 */
public class CollectionService implements AddPokemonToCollectionUseCase, RemovePokemonFromCollectionUseCase,
        ListMyCollectionUseCase {

    private final CollectionRepository collectionRepository;
    private final PokemonCatalogLookupPort pokemonCatalogLookupPort;

    public CollectionService(CollectionRepository collectionRepository,
                              PokemonCatalogLookupPort pokemonCatalogLookupPort) {
        this.collectionRepository = collectionRepository;
        this.pokemonCatalogLookupPort = pokemonCatalogLookupPort;
    }

    @Override
    public CollectionEntryView add(AddPokemonCommand command) {
        PokemonId pokemonId = new PokemonId(command.pokemonId());

        // Fail fast on the common case (already caught this one) without
        // loading the whole collection, and without a wasted catalog lookup.
        if (collectionRepository.existsEntry(command.trainerId(), pokemonId)) {
            throw new PokemonAlreadyInCollectionException(pokemonId);
        }

        PokemonCatalogLookupPort.PokemonSnapshot snapshot = pokemonCatalogLookupPort.lookup(pokemonId)
                .orElseThrow(() -> new UnknownPokemonException(pokemonId));

        Collection collection = collectionRepository.findByTrainerId(command.trainerId())
                .orElseGet(() -> Collection.createEmpty(command.trainerId()));

        CollectionEntry entry = collection.addPokemon(pokemonId, snapshot.name(), snapshot.spriteUrl());

        collectionRepository.save(collection);
        return toView(entry);
    }

    @Override
    public void remove(RemovePokemonCommand command) {
        Collection collection = collectionRepository.findByTrainerId(command.trainerId())
                .orElseThrow(() -> new PokemonNotInCollectionException(new PokemonId(command.pokemonId())));

        collection.removePokemon(new PokemonId(command.pokemonId()));
        collectionRepository.save(collection);
    }

    @Override
    public List<CollectionEntryView> list(TrainerId trainerId) {
        return collectionRepository.findByTrainerId(trainerId)
                .map(collection -> collection.entries().stream()
                        .map(this::toView)
                        .sorted((a, b) -> b.caughtAt().compareTo(a.caughtAt()))
                        .toList())
                .orElse(List.of());
    }

    private CollectionEntryView toView(CollectionEntry entry) {
        return new CollectionEntryView(entry.pokemonId().value(), entry.pokemonName(), entry.spriteUrl(),
                entry.caughtAt());
    }
}
