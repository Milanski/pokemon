package com.mbls.challenge.pokemon.catalog.application;

import com.mbls.challenge.pokemon.catalog.domain.exception.PokemonNotFoundException;
import com.mbls.challenge.pokemon.catalog.domain.model.PokemonDetails;
import com.mbls.challenge.pokemon.catalog.domain.model.PokemonSummary;
import com.mbls.challenge.pokemon.catalog.application.port.in.ListPokemonUseCase.ListPokemonQuery;
import com.mbls.challenge.pokemon.catalog.domain.port.out.PokemonCatalogPort;
import com.mbls.challenge.pokemon.shared.domain.PageResult;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PokemonCatalogServiceTest {

    @Mock
    private PokemonCatalogPort pokemonCatalogPort;

    private PokemonCatalogService service;

    @BeforeEach
    void setUp() {
        service = new PokemonCatalogService(pokemonCatalogPort);
    }

    private List<PokemonSummary> threeSamplePokemon() {
        return List.of(
                new PokemonSummary(new PokemonId(1), "bulbasaur", "http://s/1.png"),
                new PokemonSummary(new PokemonId(4), "charmander", "http://s/4.png"),
                new PokemonSummary(new PokemonId(25), "pikachu", "http://s/25.png")
        );
    }

    @Test
    void listsAPageOfPokemon() {
        when(pokemonCatalogPort.fetchAllSummaries()).thenReturn(threeSamplePokemon());

        PageResult<PokemonSummary> result = service.list(new ListPokemonQuery(0, 2, null));

        assertThat(result.content()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(3);
        assertThat(result.totalPages()).isEqualTo(2);
    }

    @Test
    void secondPageContainsTheRemainder() {
        when(pokemonCatalogPort.fetchAllSummaries()).thenReturn(threeSamplePokemon());

        PageResult<PokemonSummary> result = service.list(new ListPokemonQuery(1, 2, null));

        assertThat(result.content()).extracting(PokemonSummary::name).containsExactly("pikachu");
    }

    @Test
    void filtersByNameCaseInsensitively() {
        when(pokemonCatalogPort.fetchAllSummaries()).thenReturn(threeSamplePokemon());

        PageResult<PokemonSummary> result = service.list(new ListPokemonQuery(0, 20, "CHAR"));

        assertThat(result.content()).extracting(PokemonSummary::name).containsExactly("charmander");
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void gettingAnUnknownPokemonThrows() {
        PokemonId id = new PokemonId(99999);
        when(pokemonCatalogPort.fetchDetails(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id)).isInstanceOf(PokemonNotFoundException.class);
    }

    @Test
    void gettingAKnownPokemonReturnsItsDetails() {
        PokemonId id = new PokemonId(25);
        PokemonDetails details = new PokemonDetails(id, "pikachu", "http://s/25.png", List.of("electric"),
                4, 60, 112, List.of());
        when(pokemonCatalogPort.fetchDetails(id)).thenReturn(Optional.of(details));

        assertThat(service.getById(id)).isEqualTo(details);
    }
}
