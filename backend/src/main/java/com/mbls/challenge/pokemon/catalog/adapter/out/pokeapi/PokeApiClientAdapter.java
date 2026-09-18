package com.mbls.challenge.pokemon.catalog.adapter.out.pokeapi;

import com.mbls.challenge.pokemon.catalog.adapter.out.pokeapi.dto.PokeApiListResponse;
import com.mbls.challenge.pokemon.catalog.adapter.out.pokeapi.dto.PokeApiPokemonResponse;
import com.mbls.challenge.pokemon.catalog.domain.model.PokemonDetails;
import com.mbls.challenge.pokemon.catalog.domain.model.PokemonSummary;
import com.mbls.challenge.pokemon.catalog.domain.port.out.PokemonCatalogPort;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

/**
 * Anti-corruption layer around the public PokéAPI (https://pokeapi.co). Only
 * this adapter knows about PokéAPI's JSON shape and URLs; the rest of the
 * application only ever sees the {@code catalog.domain.model} types.
 *
 * Both methods are wrapped with the "pokeApi" {@code @Retry} and {@code
 * @CircuitBreaker} instances (configured in application.yml): a handful of
 * quick retries absorb a single transient blip, and once failures pile up
 * the circuit opens so the app fails fast for a cooldown window instead of
 * piling up slow timeouts against a dependency that is genuinely down. A
 * 404 ("this Pokémon does not exist") is configured as an ignored exception
 * for both - it is a normal outcome, not a sign of an unhealthy dependency,
 * and must not count against either the retry budget or the circuit's
 * failure rate.
 */
@Component
class PokeApiClientAdapter implements PokemonCatalogPort {

    private static final String RESILIENCE_INSTANCE = "pokeApi";

    private static final Logger log = LoggerFactory.getLogger(PokeApiClientAdapter.class);
    private static final int FULL_LIST_LIMIT = 100_000;

    private final RestClient pokeApiRestClient;
    private final PokeApiMapper mapper;

    PokeApiClientAdapter(RestClient pokeApiRestClient, PokeApiMapper mapper) {
        this.pokeApiRestClient = pokeApiRestClient;
        this.mapper = mapper;
    }

    @Override
    @Cacheable("pokemonSummaries")
    @Retry(name = RESILIENCE_INSTANCE)
    @CircuitBreaker(name = RESILIENCE_INSTANCE)
    public List<PokemonSummary> fetchAllSummaries() {
        log.info("Fetching full Pokémon list from PokéAPI (cache miss)");
        PokeApiListResponse response = pokeApiRestClient.get()
                .uri("/pokemon?limit={limit}&offset=0", FULL_LIST_LIMIT)
                .retrieve()
                .body(PokeApiListResponse.class);

        if (response == null || response.results() == null) {
            return List.of();
        }
        return response.results().stream().map(mapper::toSummary).toList();
    }

    /**
     * Only a genuine 404 ("this Pokémon does not exist") is cached as absent -
     * that fact is stable forever. Any other failure (timeout, 5xx, rate
     * limiting, ...) is an infrastructure problem, not a domain fact, and
     * must propagate uncached: {@code @Cacheable} would otherwise memoize a
     * transient outage as "not found" for the lifetime of the JVM.
     */
    @Override
    @Cacheable("pokemonDetails")
    @Retry(name = RESILIENCE_INSTANCE)
    @CircuitBreaker(name = RESILIENCE_INSTANCE)
    public Optional<PokemonDetails> fetchDetails(PokemonId id) {
        log.info("Fetching Pokémon {} details from PokéAPI (cache miss)", id);
        try {
            PokeApiPokemonResponse response = pokeApiRestClient.get()
                    .uri("/pokemon/{id}", id.value())
                    .retrieve()
                    .body(PokeApiPokemonResponse.class);
            return Optional.ofNullable(response).map(mapper::toDetails);
        } catch (HttpClientErrorException.NotFound e) {
            log.info("PokéAPI reports no such pokemon: {}", id);
            return Optional.empty();
        }
    }
}
