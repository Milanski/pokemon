package com.mbls.challenge.pokemon.trainer.domain.exception;

import com.mbls.challenge.pokemon.shared.domain.DomainException;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("username or password is incorrect");
    }
}
