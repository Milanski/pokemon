package com.mbls.challenge.pokemon.shared.domain;

/**
 * A value object was constructed with input that violates its invariant
 * (e.g. a malformed email, a non-positive Pokémon id). Deliberately distinct
 * from the JDK's generic {@link IllegalArgumentException}: the web layer
 * maps this one to 400 Bad Request, but must not do the same for an
 * arbitrary {@code IllegalArgumentException} raised by unrelated code, which
 * would misreport a real bug as a client input error.
 */
public class InvalidValueException extends DomainException {

    public InvalidValueException(String message) {
        super(message);
    }
}
