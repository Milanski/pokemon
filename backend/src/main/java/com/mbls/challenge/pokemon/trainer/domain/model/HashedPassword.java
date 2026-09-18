package com.mbls.challenge.pokemon.trainer.domain.model;

import com.mbls.challenge.pokemon.shared.domain.InvalidValueException;

import java.util.Objects;

/**
 * A password that has already been hashed by a {@link
 * com.mbls.challenge.pokemon.trainer.application.port.out.PasswordHasher}. The
 * domain never sees or stores a raw password.
 */
public record HashedPassword(String value) {

    public HashedPassword {
        Objects.requireNonNull(value, "hashed password must not be null");
        if (value.isBlank()) {
            throw new InvalidValueException("hashed password must not be blank");
        }
    }
}
