package com.mbls.challenge.pokemon.collection.adapter.out.persistence;

import com.mbls.challenge.pokemon.collection.domain.exception.CollectionConflictException;
import com.mbls.challenge.pokemon.collection.domain.model.Collection;
import com.mbls.challenge.pokemon.collection.domain.port.out.CollectionRepository;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;
import com.mbls.challenge.pokemon.shared.domain.TrainerId;
import com.mbls.challenge.pokemon.trainer.domain.model.Email;
import com.mbls.challenge.pokemon.trainer.domain.model.HashedPassword;
import com.mbls.challenge.pokemon.trainer.domain.model.Trainer;
import com.mbls.challenge.pokemon.trainer.domain.model.Username;
import com.mbls.challenge.pokemon.trainer.domain.port.out.TrainerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Reproduces the exact scenario the code review flagged as CRITICAL: two
 * requests read the same collection, each mutate their own in-memory copy,
 * and both try to save. Before the version check existed, the second save
 * silently discarded the first request's change; now it must be rejected.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:collection_repository_adapter_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.jwt.secret=test-only-secret-must-be-at-least-32-bytes-long"
})
class CollectionRepositoryAdapterTest {

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    /** Collections have a foreign key to trainers, so tests need a real trainer row. */
    private TrainerId aRegisteredTrainerId() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        Trainer trainer = Trainer.register(new Username("t" + unique), new Email(unique + "@pallet.town"),
                new HashedPassword("hash"));
        return trainerRepository.save(trainer).id();
    }

    @Test
    void savingFromAStaleSnapshotIsRejectedInsteadOfSilentlyDroppingTheOtherRequestsEntry() {
        TrainerId trainerId = aRegisteredTrainerId();
        collectionRepository.save(Collection.createEmpty(trainerId));

        // Two "concurrent" requests each read the same starting state.
        Collection readerA = collectionRepository.findByTrainerId(trainerId).orElseThrow();
        Collection readerB = collectionRepository.findByTrainerId(trainerId).orElseThrow();

        readerA.addPokemon(new PokemonId(1), "bulbasaur", "http://sprite/1.png");
        collectionRepository.save(readerA);

        readerB.addPokemon(new PokemonId(4), "charmander", "http://sprite/4.png");

        assertThatThrownBy(() -> collectionRepository.save(readerB))
                .isInstanceOf(CollectionConflictException.class);

        // Bulbasaur must still be there - not silently deleted by B's stale write.
        Collection reloaded = collectionRepository.findByTrainerId(trainerId).orElseThrow();
        assertThat(reloaded.contains(new PokemonId(1))).isTrue();
        assertThat(reloaded.contains(new PokemonId(4))).isFalse();
    }

    @Test
    void twoFirstTimeAddsForTheSameTrainerRaceOnCollectionCreationSafely() {
        TrainerId trainerId = aRegisteredTrainerId();

        Collection firstAttempt = Collection.createEmpty(trainerId);
        firstAttempt.addPokemon(new PokemonId(1), "bulbasaur", "http://sprite/1.png");
        collectionRepository.save(firstAttempt);

        // A second "trainer has no collection yet" attempt with its own new
        // aggregate id races against the one just saved above.
        Collection secondAttempt = Collection.createEmpty(trainerId);
        secondAttempt.addPokemon(new PokemonId(4), "charmander", "http://sprite/4.png");

        assertThatThrownBy(() -> collectionRepository.save(secondAttempt))
                .isInstanceOf(CollectionConflictException.class);
    }

    @Test
    void savingWithTheCurrentVersionSucceeds() {
        TrainerId trainerId = aRegisteredTrainerId();
        Collection collection = collectionRepository.save(Collection.createEmpty(trainerId));

        collection.addPokemon(new PokemonId(25), "pikachu", "http://sprite/25.png");
        collectionRepository.save(collection);

        Collection reloaded = collectionRepository.findByTrainerId(trainerId).orElseThrow();
        assertThat(reloaded.contains(new PokemonId(25))).isTrue();
    }

    @Test
    void addingToACollectionThatAlreadyHasEntriesKeepsTheExistingOnes() {
        TrainerId trainerId = aRegisteredTrainerId();
        Collection collection = collectionRepository.save(Collection.createEmpty(trainerId));

        collection.addPokemon(new PokemonId(1), "bulbasaur", "http://sprite/1.png");
        collection = collectionRepository.save(collection);

        collection.addPokemon(new PokemonId(4), "charmander", "http://sprite/4.png");
        collectionRepository.save(collection);

        Collection reloaded = collectionRepository.findByTrainerId(trainerId).orElseThrow();
        assertThat(reloaded.entries()).hasSize(2);
        assertThat(reloaded.contains(new PokemonId(1))).isTrue();
        assertThat(reloaded.contains(new PokemonId(4))).isTrue();
    }

    @Test
    void removingOneEntryLeavesTheOthersIntact() {
        TrainerId trainerId = aRegisteredTrainerId();
        Collection collection = collectionRepository.save(Collection.createEmpty(trainerId));
        collection.addPokemon(new PokemonId(1), "bulbasaur", "http://sprite/1.png");
        collection.addPokemon(new PokemonId(4), "charmander", "http://sprite/4.png");
        collection.addPokemon(new PokemonId(7), "squirtle", "http://sprite/7.png");
        collection = collectionRepository.save(collection);

        collection.removePokemon(new PokemonId(4));
        collectionRepository.save(collection);

        Collection reloaded = collectionRepository.findByTrainerId(trainerId).orElseThrow();
        assertThat(reloaded.entries()).hasSize(2);
        assertThat(reloaded.contains(new PokemonId(1))).isTrue();
        assertThat(reloaded.contains(new PokemonId(4))).isFalse();
        assertThat(reloaded.contains(new PokemonId(7))).isTrue();
    }

    @Test
    void existsEntryAnswersWithoutRequiringTheCallerToLoadTheWholeCollection() {
        TrainerId trainerId = aRegisteredTrainerId();
        Collection collection = collectionRepository.save(Collection.createEmpty(trainerId));
        collection.addPokemon(new PokemonId(25), "pikachu", "http://sprite/25.png");
        collectionRepository.save(collection);

        assertThat(collectionRepository.existsEntry(trainerId, new PokemonId(25))).isTrue();
        assertThat(collectionRepository.existsEntry(trainerId, new PokemonId(1))).isFalse();
    }
}
