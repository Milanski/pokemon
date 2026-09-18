package com.mbls.challenge.pokemon.shared.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Identity of a Trainer, shared kernel between the trainer and collection
 * bounded contexts so that collection can reference its owner without
 * depending on the trainer context's domain model.
 */
public record TrainerId(UUID value) {

    public TrainerId {
        Objects.requireNonNull(value, "trainer id must not be null");
    }

    public static TrainerId newId() {
        return new TrainerId(UUID.randomUUID());
    }

    public static TrainerId of(String raw) {
        return new TrainerId(UUID.fromString(raw));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
