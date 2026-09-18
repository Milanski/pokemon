package com.mbls.challenge.pokemon.collection.domain.model;

import java.util.Objects;
import java.util.UUID;

public record CollectionEntryId(UUID value) {

    public CollectionEntryId {
        Objects.requireNonNull(value);
    }

    public static CollectionEntryId newId() {
        return new CollectionEntryId(UUID.randomUUID());
    }
}
