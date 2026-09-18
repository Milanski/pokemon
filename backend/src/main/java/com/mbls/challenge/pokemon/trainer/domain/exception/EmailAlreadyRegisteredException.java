package com.mbls.challenge.pokemon.trainer.domain.exception;

import com.mbls.challenge.pokemon.shared.domain.DomainException;

public class EmailAlreadyRegisteredException extends DomainException {

    public EmailAlreadyRegisteredException(String email) {
        super("email '" + email + "' is already registered");
    }
}
