package com.mbls.challenge.pokemon.trainer.adapter.in.web;

import com.mbls.challenge.pokemon.trainer.adapter.in.web.dto.AuthResponse;
import com.mbls.challenge.pokemon.trainer.adapter.in.web.dto.LoginRequest;
import com.mbls.challenge.pokemon.trainer.adapter.in.web.dto.RegisterRequest;
import com.mbls.challenge.pokemon.trainer.application.port.in.LoginTrainerUseCase;
import com.mbls.challenge.pokemon.trainer.application.port.in.LoginTrainerUseCase.LoginCommand;
import com.mbls.challenge.pokemon.trainer.application.port.in.RegisterTrainerUseCase;
import com.mbls.challenge.pokemon.trainer.application.port.in.RegisterTrainerUseCase.RegisterTrainerCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterTrainerUseCase registerTrainerUseCase;
    private final LoginTrainerUseCase loginTrainerUseCase;

    public AuthController(RegisterTrainerUseCase registerTrainerUseCase, LoginTrainerUseCase loginTrainerUseCase) {
        this.registerTrainerUseCase = registerTrainerUseCase;
        this.loginTrainerUseCase = loginTrainerUseCase;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        var result = registerTrainerUseCase.register(
                new RegisterTrainerCommand(request.username(), request.email(), request.password()));
        return AuthResponse.from(result);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        var result = loginTrainerUseCase.login(new LoginCommand(request.username(), request.password()));
        return AuthResponse.from(result);
    }
}
