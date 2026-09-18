package com.mbls.challenge.pokemon.trainer.application.port.out;

import com.mbls.challenge.pokemon.trainer.domain.model.HashedPassword;

/** Outbound port so the domain can hash/verify passwords without knowing the algorithm used. */
public interface PasswordHasher {

    HashedPassword hash(String rawPassword);

    boolean matches(String rawPassword, HashedPassword hashedPassword);
}
