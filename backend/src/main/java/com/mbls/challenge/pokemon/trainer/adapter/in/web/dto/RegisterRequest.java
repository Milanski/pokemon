package com.mbls.challenge.pokemon.trainer.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String email,
        @NotBlank String password
) {
}
