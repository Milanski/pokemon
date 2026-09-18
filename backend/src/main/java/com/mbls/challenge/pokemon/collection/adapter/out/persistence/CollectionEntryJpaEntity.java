package com.mbls.challenge.pokemon.collection.adapter.out.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "collection_entries", uniqueConstraints = @UniqueConstraint(columnNames = {"collection_id", "pokemon_id"}))
public class CollectionEntryJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_id", nullable = false)
    private CollectionJpaEntity collection;

    @Column(name = "pokemon_id", nullable = false)
    private int pokemonId;

    @Column(name = "pokemon_name", nullable = false)
    private String pokemonName;

    @Column(name = "sprite_url")
    private String spriteUrl;

    @Column(name = "caught_at", nullable = false)
    private Instant caughtAt;

    protected CollectionEntryJpaEntity() {
        // JPA
    }

    public CollectionEntryJpaEntity(UUID id, CollectionJpaEntity collection, int pokemonId, String pokemonName,
                                     String spriteUrl, Instant caughtAt) {
        this.id = id;
        this.collection = collection;
        this.pokemonId = pokemonId;
        this.pokemonName = pokemonName;
        this.spriteUrl = spriteUrl;
        this.caughtAt = caughtAt;
    }

    public UUID getId() {
        return id;
    }

    public int getPokemonId() {
        return pokemonId;
    }

    public String getPokemonName() {
        return pokemonName;
    }

    public String getSpriteUrl() {
        return spriteUrl;
    }

    public Instant getCaughtAt() {
        return caughtAt;
    }
}
