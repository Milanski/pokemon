package com.mbls.challenge.pokemon.collection.adapter.in.web;

import com.mbls.challenge.pokemon.collection.adapter.in.web.dto.CollectionEntryResponse;
import com.mbls.challenge.pokemon.collection.application.port.in.AddPokemonToCollectionUseCase;
import com.mbls.challenge.pokemon.collection.application.port.in.AddPokemonToCollectionUseCase.AddPokemonCommand;
import com.mbls.challenge.pokemon.collection.application.port.in.ListMyCollectionUseCase;
import com.mbls.challenge.pokemon.collection.application.port.in.RemovePokemonFromCollectionUseCase;
import com.mbls.challenge.pokemon.collection.application.port.in.RemovePokemonFromCollectionUseCase.RemovePokemonCommand;
import com.mbls.challenge.pokemon.shared.security.TrainerPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collection")
public class CollectionController {

    private final AddPokemonToCollectionUseCase addPokemonToCollectionUseCase;
    private final RemovePokemonFromCollectionUseCase removePokemonFromCollectionUseCase;
    private final ListMyCollectionUseCase listMyCollectionUseCase;

    public CollectionController(AddPokemonToCollectionUseCase addPokemonToCollectionUseCase,
                                 RemovePokemonFromCollectionUseCase removePokemonFromCollectionUseCase,
                                 ListMyCollectionUseCase listMyCollectionUseCase) {
        this.addPokemonToCollectionUseCase = addPokemonToCollectionUseCase;
        this.removePokemonFromCollectionUseCase = removePokemonFromCollectionUseCase;
        this.listMyCollectionUseCase = listMyCollectionUseCase;
    }

    @GetMapping
    public List<CollectionEntryResponse> myCollection(@AuthenticationPrincipal TrainerPrincipal principal) {
        return listMyCollectionUseCase.list(principal.trainerId()).stream()
                .map(CollectionEntryResponse::from)
                .toList();
    }

    @PostMapping("/{pokemonId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CollectionEntryResponse add(@AuthenticationPrincipal TrainerPrincipal principal,
                                        @PathVariable int pokemonId) {
        var entry = addPokemonToCollectionUseCase.add(new AddPokemonCommand(principal.trainerId(), pokemonId));
        return CollectionEntryResponse.from(entry);
    }

    @DeleteMapping("/{pokemonId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@AuthenticationPrincipal TrainerPrincipal principal, @PathVariable int pokemonId) {
        removePokemonFromCollectionUseCase.remove(new RemovePokemonCommand(principal.trainerId(), pokemonId));
    }
}
