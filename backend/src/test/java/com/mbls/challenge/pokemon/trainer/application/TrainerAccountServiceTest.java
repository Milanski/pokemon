package com.mbls.challenge.pokemon.trainer.application;

import com.mbls.challenge.pokemon.trainer.domain.exception.EmailAlreadyRegisteredException;
import com.mbls.challenge.pokemon.trainer.domain.exception.InvalidCredentialsException;
import com.mbls.challenge.pokemon.trainer.domain.exception.UsernameAlreadyTakenException;
import com.mbls.challenge.pokemon.trainer.domain.exception.WeakPasswordException;
import com.mbls.challenge.pokemon.trainer.domain.model.HashedPassword;
import com.mbls.challenge.pokemon.trainer.domain.model.Trainer;
import com.mbls.challenge.pokemon.trainer.application.port.in.AuthenticatedTrainer;
import com.mbls.challenge.pokemon.trainer.application.port.in.LoginTrainerUseCase.LoginCommand;
import com.mbls.challenge.pokemon.trainer.application.port.in.RegisterTrainerUseCase.RegisterTrainerCommand;
import com.mbls.challenge.pokemon.trainer.application.port.out.PasswordHasher;
import com.mbls.challenge.pokemon.trainer.application.port.out.TokenIssuer;
import com.mbls.challenge.pokemon.trainer.domain.port.out.TrainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerAccountServiceTest {

    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private TokenIssuer tokenIssuer;

    private TrainerAccountService service;

    @BeforeEach
    void setUp() {
        service = new TrainerAccountService(trainerRepository, passwordHasher, tokenIssuer);
    }

    @Test
    void registeringANewTrainerHashesThePasswordAndIssuesAToken() {
        when(trainerRepository.existsByUsername("ash")).thenReturn(false);
        when(trainerRepository.existsByEmail("ash@pallet.town")).thenReturn(false);
        when(passwordHasher.hash("trainerpw1")).thenReturn(new HashedPassword("hashed-value"));
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenIssuer.issueFor(any(Trainer.class))).thenReturn("jwt-token");

        AuthenticatedTrainer result = service.register(new RegisterTrainerCommand("ash", "ash@pallet.town", "trainerpw1"));

        assertThat(result.username()).isEqualTo("ash");
        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    void registeringWithATakenUsernameFails() {
        when(trainerRepository.existsByUsername("ash")).thenReturn(true);

        assertThatThrownBy(() -> service.register(new RegisterTrainerCommand("ash", "ash@pallet.town", "trainerpw1")))
                .isInstanceOf(UsernameAlreadyTakenException.class);
    }

    @Test
    void registeringWithAnAlreadyRegisteredEmailFails() {
        when(trainerRepository.existsByUsername("ash")).thenReturn(false);
        when(trainerRepository.existsByEmail("ash@pallet.town")).thenReturn(true);

        assertThatThrownBy(() -> service.register(new RegisterTrainerCommand("ash", "ash@pallet.town", "trainerpw1")))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }

    @Test
    void registeringWithATooShortPasswordFails() {
        assertThatThrownBy(() -> service.register(new RegisterTrainerCommand("ash", "ash@pallet.town", "short")))
                .isInstanceOf(WeakPasswordException.class);
    }

    @Test
    void loggingInWithCorrectCredentialsIssuesAToken() {
        Trainer trainer = Trainer.register(
                new com.mbls.challenge.pokemon.trainer.domain.model.Username("ash"),
                new com.mbls.challenge.pokemon.trainer.domain.model.Email("ash@pallet.town"),
                new HashedPassword("hashed-value"));
        when(trainerRepository.findByUsername("ash")).thenReturn(Optional.of(trainer));
        when(passwordHasher.matches("trainerpw1", trainer.hashedPassword())).thenReturn(true);
        when(tokenIssuer.issueFor(trainer)).thenReturn("jwt-token");

        AuthenticatedTrainer result = service.login(new LoginCommand("ash", "trainerpw1"));

        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    void loggingInWithAWrongPasswordFails() {
        Trainer trainer = Trainer.register(
                new com.mbls.challenge.pokemon.trainer.domain.model.Username("ash"),
                new com.mbls.challenge.pokemon.trainer.domain.model.Email("ash@pallet.town"),
                new HashedPassword("hashed-value"));
        when(trainerRepository.findByUsername("ash")).thenReturn(Optional.of(trainer));
        when(passwordHasher.matches("wrong", trainer.hashedPassword())).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginCommand("ash", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loggingInWithAnUnknownUsernameFails() {
        when(trainerRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginCommand("nobody", "whatever1")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
