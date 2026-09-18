import type { PokemonSummary } from '../api/types';

interface Props {
  pokemon: PokemonSummary;
  owned: boolean;
  onAdd: (id: number) => void;
  onRemove: (id: number) => void;
  busy: boolean;
}

export function PokemonCard({ pokemon, owned, onAdd, onRemove, busy }: Props) {
  return (
    <article className="pokemon-card">
      <span className="pokemon-card__id">#{String(pokemon.id).padStart(3, '0')}</span>
      <img src={pokemon.spriteUrl} alt={pokemon.name} loading="lazy" />
      <div className="pokemon-card__body">
        <h3>{pokemon.name}</h3>
        {owned ? (
          <button className="secondary" disabled={busy} onClick={() => onRemove(pokemon.id)}>
            Remove
          </button>
        ) : (
          <button disabled={busy} onClick={() => onAdd(pokemon.id)}>
            Add to collection
          </button>
        )}
      </div>
    </article>
  );
}
