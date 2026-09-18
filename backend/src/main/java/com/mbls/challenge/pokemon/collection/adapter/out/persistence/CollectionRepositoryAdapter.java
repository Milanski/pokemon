package com.mbls.challenge.pokemon.collection.adapter.out.persistence;

import com.mbls.challenge.pokemon.collection.domain.exception.CollectionConflictException;
import com.mbls.challenge.pokemon.collection.domain.model.Collection;
import com.mbls.challenge.pokemon.collection.domain.model.CollectionEntry;
import com.mbls.challenge.pokemon.collection.domain.model.CollectionEntryId;
import com.mbls.challenge.pokemon.collection.domain.model.CollectionId;
import com.mbls.challenge.pokemon.collection.domain.port.out.CollectionRepository;
import com.mbls.challenge.pokemon.shared.domain.PokemonId;
import com.mbls.challenge.pokemon.shared.domain.TrainerId;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Transactional
class CollectionRepositoryAdapter implements CollectionRepository {

    private final SpringDataCollectionRepository springDataRepository;
    private final SpringDataCollectionEntryRepository springDataEntryRepository;

    CollectionRepositoryAdapter(SpringDataCollectionRepository springDataRepository,
                                 SpringDataCollectionEntryRepository springDataEntryRepository) {
        this.springDataRepository = springDataRepository;
        this.springDataEntryRepository = springDataEntryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Collection> findByTrainerId(TrainerId trainerId) {
        return springDataRepository.findByTrainerId(trainerId.value()).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsEntry(TrainerId trainerId, PokemonId pokemonId) {
        return springDataEntryRepository.existsByCollection_TrainerIdAndPokemonId(trainerId.value(), pokemonId.value());
    }

    @Override
    public Collection save(Collection collection) {
        try {
            CollectionJpaEntity entity = springDataRepository.findById(collection.id().value())
                    .orElseGet(() -> new CollectionJpaEntity(collection.id().value(), collection.trainerId().value()));

            if (entity.getVersion() != collection.version()) {
                throw new CollectionConflictException();
            }

            Set<UUID> domainEntryIds = collection.entries().stream()
                    .map(entry -> entry.id().value())
                    .collect(Collectors.toSet());

            entity.getEntries().removeIf(jpaEntry -> !domainEntryIds.contains(jpaEntry.getId()));

            Set<UUID> persistedIds = entity.getEntries().stream()
                    .map(CollectionEntryJpaEntity::getId)
                    .collect(Collectors.toSet());

            for (CollectionEntry domainEntry : collection.entries()) {
                if (!persistedIds.contains(domainEntry.id().value())) {
                    entity.getEntries().add(new CollectionEntryJpaEntity(
                            domainEntry.id().value(),
                            entity,
                            domainEntry.pokemonId().value(),
                            domainEntry.pokemonName(),
                            domainEntry.spriteUrl(),
                            domainEntry.caughtAt()
                    ));
                }
            }

            entity.setVersion(entity.getVersion() + 1);
            CollectionJpaEntity saved = springDataRepository.saveAndFlush(entity);
            return toDomain(saved);
        } catch (DataIntegrityViolationException e) {
            throw new CollectionConflictException();
        }
    }

    private Collection toDomain(CollectionJpaEntity entity) {
        Set<CollectionEntry> entries = entity.getEntries().stream()
                .map(e -> CollectionEntry.reconstitute(
                        new CollectionEntryId(e.getId()),
                        new PokemonId(e.getPokemonId()),
                        e.getPokemonName(),
                        e.getSpriteUrl(),
                        e.getCaughtAt()))
                .collect(Collectors.toSet());

        return Collection.reconstitute(new CollectionId(entity.getId()), new TrainerId(entity.getTrainerId()),
                entries, entity.getVersion());
    }
}
