package com.mbls.challenge.pokemon.collection.application.port.in;

import com.mbls.challenge.pokemon.shared.domain.TrainerId;

import java.util.List;

public interface ListMyCollectionUseCase {

    List<CollectionEntryView> list(TrainerId trainerId);
}
