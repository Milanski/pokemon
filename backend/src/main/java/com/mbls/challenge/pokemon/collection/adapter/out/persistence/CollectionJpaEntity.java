package com.mbls.challenge.pokemon.collection.adapter.out.persistence;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "collections")
public class CollectionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "trainer_id", nullable = false, unique = true)
    private UUID trainerId;

    /**
     * Optimistic-locking version, managed explicitly by {@link
     * CollectionRepositoryAdapter} rather than JPA's {@code @Version}: this
     * entity's only mutable state ({@code entries}) lives on the *inverse*
     * side of a {@code mappedBy} association, and Hibernate does not bump an
     * owning entity's {@code @Version} for changes that only touch the
     * inverse side of such a relationship - so an automatic {@code @Version}
     * here would silently never increment.
     */
    @Column(name = "version", nullable = false)
    private long version;

    // LAZY: a caller that only needs existsEntry()/version - not the full
    // entry graph - shouldn't pay for loading it. Every current call site
    // that loads a Collection does need the entries and triggers this within
    // an open transaction, so this mainly guards against a future caller
    // that doesn't.
    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<CollectionEntryJpaEntity> entries = new HashSet<>();

    protected CollectionJpaEntity() {
        // JPA
    }

    public CollectionJpaEntity(UUID id, UUID trainerId) {
        this.id = id;
        this.trainerId = trainerId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTrainerId() {
        return trainerId;
    }

    public long getVersion() {
        return version;
    }

    void setVersion(long version) {
        this.version = version;
    }

    public Set<CollectionEntryJpaEntity> getEntries() {
        return entries;
    }
}
