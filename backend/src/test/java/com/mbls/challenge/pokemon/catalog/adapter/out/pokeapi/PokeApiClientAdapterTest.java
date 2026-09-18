package com.mbls.challenge.pokemon.catalog.adapter.out.pokeapi;

import com.mbls.challenge.pokemon.catalog.adapter.out.pokeapi.dto.PokeApiPokemonResponse;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies that only a genuine "PokéAPI says this id doesn't exist" (404) is
 * treated as an absent Pokémon; any other failure must propagate so it is
 * never memoized by {@code @Cacheable} as a permanent "not found".
 */
class PokeApiClientAdapterTest {

    private final RestClient restClient = mock(RestClient.class, org.mockito.Answers.RETURNS_DEEP_STUBS);
    private final PokeApiClientAdapter adapter = new PokeApiClientAdapter(restClient, new PokeApiMapper());

    @Test
    void aGenuine404IsTreatedAsAnAbsentPokemon() {
        when(restClient.get().uri("/pokemon/{id}", 99999).retrieve().body(PokeApiPokemonResponse.class))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null));

        Optional<?> result = adapter.fetchDetails(new PokemonId(99999));

        assertThat(result).isEmpty();
    }

    @Test
    void aTransientFailurePropagatesInsteadOfBeingCachedAsNotFound() {
        when(restClient.get().uri("/pokemon/{id}", 25).retrieve().body(PokeApiPokemonResponse.class))
                .thenThrow(new ResourceAccessException("read timed out"));

        assertThatThrownBy(() -> adapter.fetchDetails(new PokemonId(25)))
                .isInstanceOf(ResourceAccessException.class);
    }

    @Test
    void aServerErrorPropagatesInsteadOfBeingCachedAsNotFound() {
        when(restClient.get().uri("/pokemon/{id}", 25).retrieve().body(PokeApiPokemonResponse.class))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", HttpHeaders.EMPTY, new byte[0], null));

        assertThatThrownBy(() -> adapter.fetchDetails(new PokemonId(25)))
                .isInstanceOf(HttpClientErrorException.class);
    }
}
