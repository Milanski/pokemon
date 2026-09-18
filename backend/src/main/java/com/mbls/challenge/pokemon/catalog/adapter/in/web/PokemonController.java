package com.mbls.challenge.pokemon.catalog.adapter.in.web;

import com.mbls.challenge.pokemon.catalog.adapter.in.web.dto.PokemonDetailsResponse;
import com.mbls.challenge.pokemon.catalog.adapter.in.web.dto.PokemonSummaryResponse;
import com.mbls.challenge.pokemon.catalog.application.port.in.GetPokemonDetailsUseCase;
import com.mbls.challenge.pokemon.catalog.application.port.in.ListPokemonUseCase;
import com.mbls.challenge.pokemon.catalog.application.port.in.ListPokemonUseCase.ListPokemonQuery;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;
import com.mbls.challenge.pokemon.shared.web.PageResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {

    private final ListPokemonUseCase listPokemonUseCase;
    private final GetPokemonDetailsUseCase getPokemonDetailsUseCase;

    public PokemonController(ListPokemonUseCase listPokemonUseCase, GetPokemonDetailsUseCase getPokemonDetailsUseCase) {
        this.listPokemonUseCase = listPokemonUseCase;
        this.getPokemonDetailsUseCase = getPokemonDetailsUseCase;
    }

    @GetMapping
    public PageResponse<PokemonSummaryResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        var result = listPokemonUseCase.list(new ListPokemonQuery(page, size, search));
        return PageResponse.from(result, PokemonSummaryResponse::from);
    }

    @GetMapping("/{id}")
    public PokemonDetailsResponse getById(@PathVariable int id) {
        var details = getPokemonDetailsUseCase.getById(new PokemonId(id));
        return PokemonDetailsResponse.from(details);
    }
}
