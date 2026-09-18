package com.mbls.challenge.pokemon.trainer.application.port.out;

import com.mbls.challenge.pokemon.trainer.domain.model.Trainer;

/** Outbound port for issuing an access token for an authenticated trainer. */
public interface TokenIssuer {

    String issueFor(Trainer trainer);
}
