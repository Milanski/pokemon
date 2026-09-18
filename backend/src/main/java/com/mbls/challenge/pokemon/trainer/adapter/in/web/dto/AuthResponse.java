package com.mbls.challenge.pokemon.trainer.adapter.in.web.dto;

import com.mbls.challenge.pokemon.trainer.application.port.in.AuthenticatedTrainer;

public record AuthResponse(String trainerId, String username, String token) {

    public static AuthResponse from(AuthenticatedTrainer authenticatedTrainer) {
        return new AuthResponse(
                authenticatedTrainer.trainerId().value().toString(),
                authenticatedTrainer.username(),
                authenticatedTrainer.token()
        );
    }
}
