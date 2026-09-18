package com.mbls.challenge.pokemon.collection.adapter.in.web;

import com.mbls.challenge.pokemon.collection.domain.exception.PokemonAlreadyInCollectionException;
import com.mbls.challenge.pokemon.collection.domain.exception.UnknownPokemonException;
import com.mbls.challenge.pokemon.collection.application.port.in.AddPokemonToCollectionUseCase;
import com.mbls.challenge.pokemon.collection.application.port.in.CollectionEntryView;
import com.mbls.challenge.pokemon.collection.application.port.in.ListMyCollectionUseCase;
import com.mbls.challenge.pokemon.collection.application.port.in.RemovePokemonFromCollectionUseCase;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;
import com.mbls.challenge.pokemon.shared.domain.TrainerId;
import com.mbls.challenge.pokemon.shared.security.JwtAuthenticationFilter;
import com.mbls.challenge.pokemon.shared.security.RateLimitingFilter;
import com.mbls.challenge.pokemon.shared.security.TrainerPrincipal;
import com.mbls.challenge.pokemon.shared.web.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security filters are disabled for this slice test (addFilters = false) so
 * it can focus purely on request/response mapping without wiring the whole
 * JwtAuthenticationFilter/JwtService chain. The authenticated principal is
 * therefore pushed into the SecurityContext directly rather than via a real
 * token; real end-to-end authentication is covered by
 * {@link com.mbls.challenge.pokemon.PokemonCollectionIntegrationTest}.
 *
 * The controller no longer depends on the catalog context at all (that
 * orchestration moved into {@code CollectionService}), so this test only
 * needs the three collection use cases.
 */
@WebMvcTest(CollectionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class CollectionControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddPokemonToCollectionUseCase addPokemonToCollectionUseCase;
    @MockBean
    private RemovePokemonFromCollectionUseCase removePokemonFromCollectionUseCase;
    @MockBean
    private ListMyCollectionUseCase listMyCollectionUseCase;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    private RateLimitingFilter rateLimitingFilter;

    private final TrainerId trainerId = TrainerId.newId();
    private final TrainerPrincipal principal = new TrainerPrincipal(trainerId, "ash");

    @BeforeEach
    void authenticateAsAsh() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listingReturnsTheTrainersCollection() throws Exception {
        when(listMyCollectionUseCase.list(trainerId))
                .thenReturn(List.of(new CollectionEntryView(25, "pikachu", "http://s/25.png", Instant.now())));

        mockMvc.perform(get("/api/collection"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pokemonName").value("pikachu"));
    }

    @Test
    void addingDelegatesToTheUseCaseWithTheAuthenticatedTrainer() throws Exception {
        when(addPokemonToCollectionUseCase.add(any()))
                .thenReturn(new CollectionEntryView(25, "pikachu", "http://s/25.png", Instant.now()));

        mockMvc.perform(post("/api/collection/25"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pokemonName").value("pikachu"));

        verify(addPokemonToCollectionUseCase).add(
                new AddPokemonToCollectionUseCase.AddPokemonCommand(trainerId, 25));
    }

    @Test
    void addingAPokemonAlreadyOwnedReturnsConflict() throws Exception {
        when(addPokemonToCollectionUseCase.add(any())).thenThrow(new PokemonAlreadyInCollectionException(new PokemonId(25)));

        mockMvc.perform(post("/api/collection/25"))
                .andExpect(status().isConflict());
    }

    @Test
    void addingAPokemonTheCatalogDoesNotRecognizeReturnsNotFound() throws Exception {
        when(addPokemonToCollectionUseCase.add(any())).thenThrow(new UnknownPokemonException(new PokemonId(99999)));

        mockMvc.perform(post("/api/collection/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removingDelegatesToTheUseCase() throws Exception {
        mockMvc.perform(delete("/api/collection/25"))
                .andExpect(status().isNoContent());

        verify(removePokemonFromCollectionUseCase).remove(
                new RemovePokemonFromCollectionUseCase.RemovePokemonCommand(trainerId, 25));
    }
}
