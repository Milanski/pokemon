package com.mbls.challenge.pokemon;

import com.mbls.challenge.pokemon.catalog.domain.model.PokemonDetails;
import com.mbls.challenge.pokemon.catalog.domain.model.PokemonSummary;
import com.mbls.challenge.pokemon.catalog.domain.port.out.PokemonCatalogPort;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end test exercising the real Spring Security / JWT filter chain and
 * a real (H2, Postgres-compatible mode) relational database with Flyway
 * migrations applied. Only the external PokéAPI is stubbed out via a
 * {@code @Primary} test double, so the test is fast, deterministic and needs
 * no network access or Docker to run.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = {
        "spring.datasource.url=jdbc:h2:mem:pokemon_collection_it;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.jwt.secret=test-only-secret-must-be-at-least-32-bytes-long"
})
@AutoConfigureMockMvc
class PokemonCollectionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class StubCatalogConfig {

        @Bean
        @Primary
        PokemonCatalogPort stubPokemonCatalogPort() {
            return new PokemonCatalogPort() {
                @Override
                public List<PokemonSummary> fetchAllSummaries() {
                    return List.of(new PokemonSummary(new PokemonId(25), "pikachu", "http://sprite/25.png"));
                }

                @Override
                public Optional<PokemonDetails> fetchDetails(PokemonId id) {
                    if (id.value() == 25) {
                        return Optional.of(new PokemonDetails(id, "pikachu", "http://sprite/25.png",
                                List.of("electric"), 4, 60, 112, List.of()));
                    }
                    return Optional.empty();
                }
            };
        }
    }

    private String registerAndGetToken(String username) throws Exception {
        String body = """
                {"username":"%s","email":"%s@pallet.town","password":"trainerpw1"}
                """.formatted(username, username);

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }

    @Test
    void trainerCanRegisterBrowsePokemonAndManageTheirCollection() throws Exception {
        String uniqueUsername = "ash" + UUID.randomUUID().toString().substring(0, 8);
        String token = registerAndGetToken(uniqueUsername);
        String authHeader = "Bearer " + token;

        mockMvc.perform(get("/api/pokemon").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("pikachu"));

        mockMvc.perform(post("/api/collection/25").header("Authorization", authHeader))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pokemonName").value("pikachu"));

        mockMvc.perform(get("/api/collection").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pokemonId").value(25));

        mockMvc.perform(post("/api/collection/25").header("Authorization", authHeader))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/collection/25").header("Authorization", authHeader))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/collection").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void collectionEndpointRejectsRequestsWithoutAToken() throws Exception {
        mockMvc.perform(get("/api/collection"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void twoTrainersNeverSeeEachOthersCollection() throws Exception {
        String tokenAsh = registerAndGetToken("ash" + UUID.randomUUID().toString().substring(0, 8));
        String tokenMisty = registerAndGetToken("misty" + UUID.randomUUID().toString().substring(0, 8));

        mockMvc.perform(post("/api/collection/25").header("Authorization", "Bearer " + tokenAsh))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/collection").header("Authorization", "Bearer " + tokenMisty))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void registeringTheSameUsernameTwiceIsRejected() throws Exception {
        String username = "duplicate" + UUID.randomUUID().toString().substring(0, 8);
        registerAndGetToken(username);

        String body = """
                {"username":"%s","email":"other-%s@pallet.town","password":"trainerpw1"}
                """.formatted(username, username);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }
}
