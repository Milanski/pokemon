package com.mbls.challenge.pokemon.shared.domain;

/**
 * Base type for all domain-level rule violations. Adapters translate these
 * into transport-specific responses (e.g. HTTP status codes) instead of the
 * domain knowing anything about HTTP.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
