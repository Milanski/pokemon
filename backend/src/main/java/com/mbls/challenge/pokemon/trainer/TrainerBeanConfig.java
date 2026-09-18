package com.mbls.challenge.pokemon.trainer;

import com.mbls.challenge.pokemon.trainer.application.port.out.PasswordHasher;
import com.mbls.challenge.pokemon.trainer.application.port.out.TokenIssuer;
import com.mbls.challenge.pokemon.trainer.domain.port.out.TrainerRepository;
import com.mbls.challenge.pokemon.trainer.application.TrainerAccountService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the trainer context's plain (framework-agnostic) application service
 * as a Spring bean, exposed through its inbound use case ports.
 */
@Configuration
class TrainerBeanConfig {

    @Bean
    TrainerAccountService trainerAccountService(TrainerRepository trainerRepository, PasswordHasher passwordHasher,
                                                 TokenIssuer tokenIssuer) {
        return new TrainerAccountService(trainerRepository, passwordHasher, tokenIssuer);
    }
}
