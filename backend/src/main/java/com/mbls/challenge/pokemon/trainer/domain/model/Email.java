package com.mbls.challenge.pokemon.trainer.domain.model;

import com.mbls.challenge.pokemon.shared.domain.InvalidValueException;

import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern SIMPLE_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public Email {
        if (value == null || !SIMPLE_EMAIL.matcher(value).matches()) {
            throw new InvalidValueException("'" + value + "' is not a valid email address");
        }
        value = value.toLowerCase();
    }

    @Override
    public String toString() {
        return value;
    }
}
