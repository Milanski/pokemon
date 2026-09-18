package com.mbls.challenge.pokemon.collection.application;

import com.mbls.challenge.pokemon.collection.domain.exception.PokemonAlreadyInCollectionException;
import com.mbls.challenge.pokemon.collection.domain.exception.PokemonNotInCollectionException;
import com.mbls.challenge.pokemon.collection.domain.exception.UnknownPokemonException;
import com.mbls.challenge.pokemon.collection.domain.model.Collection;
import com.mbls.challenge.pokemon.collection.application.port.in.AddPokemonToCollectionUseCase.AddPokemonCommand;
import com.mbls.challenge.pokemon.collection.application.port.in.CollectionEntryView;
import com.mbls.challenge.pokemon.collection.application.port.in.RemovePokemonFromCollectionUseCase.RemovePokemonCommand;
import com.mbls.challenge.pokemon.collection.domain.port.out.CollectionRepository;
import com.mbls.challenge.pokemon.collection.application.port.out.PokemonCatalogLookupPort;
import com.mbls.challenge.pokemon.collection.application.port.out.PokemonCatalogLookupPort.PokemonSnapshot;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;
import com.mbls.challenge.pokemon.shared.domain.TrainerId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private PokemonCatalogLookupPort pokemonCatalogLookupPort;

    private CollectionService collectionService;

    private final TrainerId trainerId = TrainerId.newId();
    private final PokemonId pikachu = new PokemonId(25);

    @BeforeEach
    void setUp() {
        collectionService = new CollectionService(collectionRepository, pokemonCatalogLookupPort);
    }

    @Test
    void addingAPokemonForANewTrainerCreatesTheCollectionOnTheFly() {
        when(collectionRepository.existsEntry(trainerId, pikachu)).thenReturn(false);
        when(pokemonCatalogLookupPort.lookup(pikachu))
                .thenReturn(Optional.of(new PokemonSnapshot("pikachu", "http://s/25.png")));
        when(collectionRepository.findByTrainerId(trainerId)).thenReturn(Optional.empty());
        when(collectionRepository.save(any(Collection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CollectionEntryView view = collectionService.add(new AddPokemonCommand(trainerId, 25));

        assertThat(view.pokemonId()).isEqualTo(25);
        assertThat(view.pokemonName()).isEqualTo("pikachu");

        ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        verify(collectionRepository).save(captor.capture());
        assertThat(captor.getValue().trainerId()).isEqualTo(trainerId);
    }

    @Test
    void addingAPokemonAlreadyInTheCollectionFailsFastWithoutLoadingTheCollectionOrCallingTheCatalog() {
        when(collectionRepository.existsEntry(trainerId, pikachu)).thenReturn(true);

        assertThatThrownBy(() -> collectionService.add(new AddPokemonCommand(trainerId, 25)))
                .isInstanceOf(PokemonAlreadyInCollectionException.class);

        verifyNoInteractions(pokemonCatalogLookupPort);
        verify(collectionRepository, never()).findByTrainerId(any());
        verify(collectionRepository, never()).save(any());
    }

    @Test
    void addingAPokemonTheCatalogDoesNotRecognizeFails() {
        when(collectionRepository.existsEntry(trainerId, pikachu)).thenReturn(false);
        when(pokemonCatalogLookupPort.lookup(pikachu)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionService.add(new AddPokemonCommand(trainerId, 25)))
                .isInstanceOf(UnknownPokemonException.class);

        verify(collectionRepository, never()).save(any());
    }

    @Test
    void removingAPokemonNotInAnyCollectionFails() {
        when(collectionRepository.findByTrainerId(trainerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionService.remove(new RemovePokemonCommand(trainerId, 25)))
                .isInstanceOf(PokemonNotInCollectionException.class);
    }

    @Test
    void listingReturnsEmptyForATrainerWithNoCollectionYet() {
        when(collectionRepository.findByTrainerId(trainerId)).thenReturn(Optional.empty());

        List<CollectionEntryView> entries = collectionService.list(trainerId);

        assertThat(entries).isEmpty();
    }
}
