package com.mbls.challenge.pokemon.collection.domain.model;

import java.util.Objects;
import java.util.UUID;

public record CollectionId(UUID value) {

    public CollectionId {
        Objects.requireNonNull(value);
    }

    public static CollectionId newId() {
        return new CollectionId(UUID.randomUUID());
    }
}
