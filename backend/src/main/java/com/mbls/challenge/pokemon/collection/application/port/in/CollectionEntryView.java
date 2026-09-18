package com.mbls.challenge.pokemon.collection.application.port.in;

import java.time.Instant;

/** Read model returned by the collection use cases, decoupled from the persistence/domain entity shape. */
public record CollectionEntryView(int pokemonId, String pokemonName, String spriteUrl, Instant caughtAt) {
}
