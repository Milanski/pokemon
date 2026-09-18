package com.mbls.challenge.pokemon.trainer.application;

import com.mbls.challenge.pokemon.trainer.domain.exception.EmailAlreadyRegisteredException;
import com.mbls.challenge.pokemon.trainer.domain.exception.InvalidCredentialsException;
import com.mbls.challenge.pokemon.trainer.domain.exception.UsernameAlreadyTakenException;
import com.mbls.challenge.pokemon.trainer.domain.exception.WeakPasswordException;
import com.mbls.challenge.pokemon.trainer.domain.model.Email;
import com.mbls.challenge.pokemon.trainer.domain.model.HashedPassword;
import com.mbls.challenge.pokemon.trainer.domain.model.Trainer;
import com.mbls.challenge.pokemon.trainer.domain.model.Username;
import com.mbls.challenge.pokemon.trainer.application.port.in.AuthenticatedTrainer;
import com.mbls.challenge.pokemon.trainer.application.port.in.LoginTrainerUseCase;
import com.mbls.challenge.pokemon.trainer.application.port.in.RegisterTrainerUseCase;
import com.mbls.challenge.pokemon.trainer.application.port.out.PasswordHasher;
import com.mbls.challenge.pokemon.trainer.application.port.out.TokenIssuer;
import com.mbls.challenge.pokemon.trainer.domain.port.out.TrainerRepository;

/**
 * Application service for the trainer identity context: orchestrates the
 * domain model and outbound ports to fulfil the register/login use cases.
 */
public class TrainerAccountService implements RegisterTrainerUseCase, LoginTrainerUseCase {

    private final TrainerRepository trainerRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public TrainerAccountService(TrainerRepository trainerRepository, PasswordHasher passwordHasher,
                                  TokenIssuer tokenIssuer) {
        this.trainerRepository = trainerRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public AuthenticatedTrainer register(RegisterTrainerCommand command) {
        Username username = new Username(command.username());
        Email email = new Email(command.email());
        if (command.rawPassword() == null || command.rawPassword().length() < 8) {
            throw new WeakPasswordException();
        }

        if (trainerRepository.existsByUsername(username.value())) {
            throw new UsernameAlreadyTakenException(username.value());
        }
        if (trainerRepository.existsByEmail(email.value())) {
            throw new EmailAlreadyRegisteredException(email.value());
        }

        HashedPassword hashedPassword = passwordHasher.hash(command.rawPassword());
        Trainer trainer = Trainer.register(username, email, hashedPassword);
        Trainer saved = trainerRepository.save(trainer);

        String token = tokenIssuer.issueFor(saved);
        return new AuthenticatedTrainer(saved.id(), saved.username().value(), token);
    }

    @Override
    public AuthenticatedTrainer login(LoginCommand command) {
        Trainer trainer = trainerRepository.findByUsername(command.username())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(command.rawPassword(), trainer.hashedPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = tokenIssuer.issueFor(trainer);
        return new AuthenticatedTrainer(trainer.id(), trainer.username().value(), token);
    }
}
