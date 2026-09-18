package com.mbls.challenge.pokemon.trainer.application.port.in;

public interface RegisterTrainerUseCase {

    AuthenticatedTrainer register(RegisterTrainerCommand command);

    record RegisterTrainerCommand(String username, String email, String rawPassword) {
    }
}
