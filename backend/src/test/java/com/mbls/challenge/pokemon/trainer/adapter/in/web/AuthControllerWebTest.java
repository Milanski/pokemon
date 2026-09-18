package com.mbls.challenge.pokemon.trainer.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbls.challenge.pokemon.shared.domain.TrainerId;
import com.mbls.challenge.pokemon.shared.web.GlobalExceptionHandler;
import com.mbls.challenge.pokemon.trainer.domain.exception.InvalidCredentialsException;
import com.mbls.challenge.pokemon.trainer.domain.exception.UsernameAlreadyTakenException;
import com.mbls.challenge.pokemon.trainer.application.port.in.AuthenticatedTrainer;
import com.mbls.challenge.pokemon.trainer.application.port.in.LoginTrainerUseCase;
import com.mbls.challenge.pokemon.trainer.application.port.in.RegisterTrainerUseCase;
import com.mbls.challenge.pokemon.shared.security.JwtAuthenticationFilter;
import com.mbls.challenge.pokemon.shared.security.RateLimitingFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterTrainerUseCase registerTrainerUseCase;

    @MockBean
    private LoginTrainerUseCase loginTrainerUseCase;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private RateLimitingFilter rateLimitingFilter;

    @Test
    void registerReturnsTokenOnSuccess() throws Exception {
        when(registerTrainerUseCase.register(any()))
                .thenReturn(new AuthenticatedTrainer(TrainerId.newId(), "ash", "jwt-token"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"ash","email":"ash@pallet.town","password":"trainerpw1"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("ash"))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void registerWithBlankUsernameIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"","email":"ash@pallet.town","password":"trainerpw1"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerWithATakenUsernameReturnsConflict() throws Exception {
        when(registerTrainerUseCase.register(any())).thenThrow(new UsernameAlreadyTakenException("ash"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"ash","email":"ash@pallet.town","password":"trainerpw1"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void loginWithWrongCredentialsReturnsUnauthorized() throws Exception {
        when(loginTrainerUseCase.login(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"ash","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
