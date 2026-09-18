package com.mbls.challenge.pokemon.trainer.domain.model;

import com.mbls.challenge.pokemon.shared.domain.TrainerId;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate root of the trainer identity bounded context. A Trainer is the
 * account a person uses to log in; it knows nothing about Pokémon or
 * collections.
 */
public final class Trainer {

    private final TrainerId id;
    private final Username username;
    private final Email email;
    private HashedPassword hashedPassword;
    private final Instant registeredAt;

    private Trainer(TrainerId id, Username username, Email email, HashedPassword hashedPassword, Instant registeredAt) {
        this.id = Objects.requireNonNull(id);
        this.username = Objects.requireNonNull(username);
        this.email = Objects.requireNonNull(email);
        this.hashedPassword = Objects.requireNonNull(hashedPassword);
        this.registeredAt = Objects.requireNonNull(registeredAt);
    }

    /** Registers a brand-new trainer. The password must already be hashed by the caller. */
    public static Trainer register(Username username, Email email, HashedPassword hashedPassword) {
        return new Trainer(TrainerId.newId(), username, email, hashedPassword, Instant.now());
    }

    /** Reconstitutes a trainer from persistence. */
    public static Trainer reconstitute(TrainerId id, Username username, Email email,
                                        HashedPassword hashedPassword, Instant registeredAt) {
        return new Trainer(id, username, email, hashedPassword, registeredAt);
    }

    public TrainerId id() {
        return id;
    }

    public Username username() {
        return username;
    }

    public Email email() {
        return email;
    }

    public HashedPassword hashedPassword() {
        return hashedPassword;
    }

    public Instant registeredAt() {
        return registeredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trainer trainer)) return false;
        return id.equals(trainer.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
