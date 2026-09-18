package com.mbls.challenge.pokemon.trainer.domain.exception;

import com.mbls.challenge.pokemon.shared.domain.DomainException;

public class WeakPasswordException extends DomainException {

    public WeakPasswordException() {
        super("password must be at least 8 characters long");
    }
}
