package com.mbls.challenge.pokemon.shared.security;

import com.mbls.challenge.pokemon.shared.domain.TrainerId;

/** The authenticated principal attached to the Spring Security context after a valid JWT is presented. */
public record TrainerPrincipal(TrainerId trainerId, String username) {
}
