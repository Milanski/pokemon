package com.mbls.challenge.pokemon.trainer.adapter.out.persistence;

import com.mbls.challenge.pokemon.trainer.domain.exception.EmailAlreadyRegisteredException;
import com.mbls.challenge.pokemon.trainer.domain.exception.UsernameAlreadyTakenException;
import com.mbls.challenge.pokemon.trainer.domain.model.Email;
import com.mbls.challenge.pokemon.trainer.domain.model.HashedPassword;
import com.mbls.challenge.pokemon.trainer.domain.model.Trainer;
import com.mbls.challenge.pokemon.trainer.domain.model.Username;
import com.mbls.challenge.pokemon.trainer.domain.port.out.TrainerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the real Spring Data / Postgres-compatible-H2 persistence path,
 * in particular the case a pure in-memory pre-check cannot cover: two
 * registrations for the same username/email racing each other and only one
 * of them reaching the {@code UNIQUE} constraint first.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:trainer_repository_adapter_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.jwt.secret=test-only-secret-must-be-at-least-32-bytes-long"
})
class TrainerRepositoryAdapterTest {

    @Autowired
    private TrainerRepository trainerRepository;

    @Test
    void aSecondSaveWithAUsernameThatWonTheRaceIsTranslatedToADomainException() {
        Username username = new Username("racecondition");
        trainerRepository.save(Trainer.register(username, new Email("first@pallet.town"), new HashedPassword("h1")));

        Trainer duplicate = Trainer.register(username, new Email("second@pallet.town"), new HashedPassword("h2"));

        assertThatThrownBy(() -> trainerRepository.save(duplicate))
                .isInstanceOf(UsernameAlreadyTakenException.class);
    }

    @Test
    void aSecondSaveWithAnEmailThatWonTheRaceIsTranslatedToADomainException() {
        Email email = new Email("shared@pallet.town");
        trainerRepository.save(Trainer.register(new Username("firsttrainer"), email, new HashedPassword("h1")));

        Trainer duplicate = Trainer.register(new Username("secondtrainer"), email, new HashedPassword("h2"));

        assertThatThrownBy(() -> trainerRepository.save(duplicate))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }
}
