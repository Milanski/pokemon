package com.mbls.challenge.pokemon.collection.domain.model;

import com.mbls.challenge.pokemon.collection.domain.exception.PokemonAlreadyInCollectionException;
import com.mbls.challenge.pokemon.collection.domain.exception.PokemonNotInCollectionException;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;
import com.mbls.challenge.pokemon.shared.domain.TrainerId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectionTest {

    private final TrainerId trainerId = TrainerId.newId();

    @Test
    void addingAPokemonMakesItPartOfTheCollection() {
        Collection collection = Collection.createEmpty(trainerId);

        collection.addPokemon(new PokemonId(25), "pikachu", "http://sprite/25.png");

        assertThat(collection.contains(new PokemonId(25))).isTrue();
        assertThat(collection.entries()).hasSize(1);
    }

    @Test
    void addingTheSamePokemonTwiceIsRejected() {
        Collection collection = Collection.createEmpty(trainerId);
        collection.addPokemon(new PokemonId(25), "pikachu", "http://sprite/25.png");

        assertThatThrownBy(() -> collection.addPokemon(new PokemonId(25), "pikachu", "http://sprite/25.png"))
                .isInstanceOf(PokemonAlreadyInCollectionException.class);
    }

    @Test
    void removingAPokemonThatIsPresentTakesItOutOfTheCollection() {
        Collection collection = Collection.createEmpty(trainerId);
        collection.addPokemon(new PokemonId(25), "pikachu", "http://sprite/25.png");

        collection.removePokemon(new PokemonId(25));

        assertThat(collection.contains(new PokemonId(25))).isFalse();
        assertThat(collection.entries()).isEmpty();
    }

    @Test
    void removingAPokemonThatIsNotPresentIsRejected() {
        Collection collection = Collection.createEmpty(trainerId);

        assertThatThrownBy(() -> collection.removePokemon(new PokemonId(25)))
                .isInstanceOf(PokemonNotInCollectionException.class);
    }

    @Test
    void differentTrainersCanEachHaveTheirOwnCollection() {
        Collection collectionA = Collection.createEmpty(TrainerId.newId());
        Collection collectionB = Collection.createEmpty(TrainerId.newId());

        collectionA.addPokemon(new PokemonId(1), "bulbasaur", "http://sprite/1.png");

        assertThat(collectionA.contains(new PokemonId(1))).isTrue();
        assertThat(collectionB.contains(new PokemonId(1))).isFalse();
    }
}
