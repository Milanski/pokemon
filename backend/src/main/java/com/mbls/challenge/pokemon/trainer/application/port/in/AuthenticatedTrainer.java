package com.mbls.challenge.pokemon.trainer.application.port.in;

import com.mbls.challenge.pokemon.shared.domain.TrainerId;

/** Result of a successful registration or login: the trainer plus a bearer token. */
public record AuthenticatedTrainer(TrainerId trainerId, String username, String token) {
}
