package com.mbls.challenge.pokemon.collection.domain.exception;

import com.mbls.challenge.pokemon.shared.domain.DomainException;

/**
 * Thrown when a collection could not be saved because another request
 * modified it in the meantime (optimistic-locking conflict). The caller
 * should reload the collection and retry rather than the write silently
 * overwriting the other request's change.
 */
public class CollectionConflictException extends DomainException {

    public CollectionConflictException() {
        super("this collection was modified by another request in the meantime - please retry");
    }
}
