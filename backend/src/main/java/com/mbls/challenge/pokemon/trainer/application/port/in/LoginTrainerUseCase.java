package com.mbls.challenge.pokemon.trainer.application.port.in;

public interface LoginTrainerUseCase {

    AuthenticatedTrainer login(LoginCommand command);

    record LoginCommand(String username, String rawPassword) {
    }
}
