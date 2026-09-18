import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { collectionApi } from '../api/collectionApi';

export function MyCollectionPage() {
  const queryClient = useQueryClient();
  const collectionQuery = useQuery({ queryKey: ['collection'], queryFn: collectionApi.list });

  const removeMutation = useMutation({
    mutationFn: collectionApi.remove,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['collection'] }),
  });

  return (
    <div className="page">
      <div className="page-header">
        <h1>My Collection</h1>
      </div>

      {collectionQuery.isLoading && <p>Loading your collection…</p>}
      {collectionQuery.isError && <p className="form-error">Could not load your collection.</p>}

      {collectionQuery.data && collectionQuery.data.length === 0 && (
        <p>
          You haven&apos;t caught any Pokémon yet. Head over to <strong>Browse</strong> to start your collection!
        </p>
      )}

      {collectionQuery.data && collectionQuery.data.length > 0 && (
        <div className="pokemon-grid">
          {collectionQuery.data.map((entry) => (
            <article className="pokemon-card" key={entry.pokemonId}>
              <span className="pokemon-card__id">#{String(entry.pokemonId).padStart(3, '0')}</span>
              <img src={entry.spriteUrl} alt={entry.pokemonName} loading="lazy" />
              <h3>{entry.pokemonName}</h3>
              <span className="caught-at">Caught {new Date(entry.caughtAt).toLocaleDateString()}</span>
              <button
                className="secondary"
                disabled={removeMutation.isPending && removeMutation.variables === entry.pokemonId}
                onClick={() => removeMutation.mutate(entry.pokemonId)}
              >
                Remove
              </button>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
