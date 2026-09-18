package com.mbls.challenge.pokemon.trainer.domain.exception;

import com.mbls.challenge.pokemon.shared.domain.DomainException;

public class UsernameAlreadyTakenException extends DomainException {

    public UsernameAlreadyTakenException(String username) {
        super("username '" + username + "' is already taken");
    }
}
