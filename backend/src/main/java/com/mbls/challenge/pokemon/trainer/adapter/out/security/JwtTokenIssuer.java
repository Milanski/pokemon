package com.mbls.challenge.pokemon.trainer.adapter.out.security;

import com.mbls.challenge.pokemon.shared.security.JwtService;
import com.mbls.challenge.pokemon.trainer.domain.model.Trainer;
import com.mbls.challenge.pokemon.trainer.application.port.out.TokenIssuer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
class JwtTokenIssuer implements TokenIssuer {

    private final JwtService jwtService;

    JwtTokenIssuer(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public String issueFor(Trainer trainer) {
        return jwtService.generateToken(
                trainer.id().value().toString(),
                Map.of("username", trainer.username().value())
        );
    }
}
