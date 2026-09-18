package com.mbls.challenge.pokemon.trainer.domain.model;

import com.mbls.challenge.pokemon.shared.domain.InvalidValueException;

import java.util.regex.Pattern;

public record Username(String value) {

    private static final Pattern ALLOWED = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    public Username {
        if (value == null || !ALLOWED.matcher(value).matches()) {
            throw new InvalidValueException(
                    "username must be 3-20 characters and contain only letters, digits or underscore");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
