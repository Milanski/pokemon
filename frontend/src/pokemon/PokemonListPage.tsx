import { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { pokemonApi } from '../api/pokemonApi';
import { collectionApi } from '../api/collectionApi';
import { PokemonCard } from './PokemonCard';

const PAGE_SIZE = 24;

export function PokemonListPage() {
  const [searchInput, setSearchInput] = useState('');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const queryClient = useQueryClient();

  useEffect(() => {
    const handle = setTimeout(() => {
      setSearch(searchInput.trim());
      setPage(0);
    }, 300);
    return () => clearTimeout(handle);
  }, [searchInput]);

  const pokemonQuery = useQuery({
    queryKey: ['pokemon', page, search],
    queryFn: () => pokemonApi.list(page, PAGE_SIZE, search),
  });

  const collectionQuery = useQuery({ queryKey: ['collection'], queryFn: collectionApi.list });

  const ownedIds = useMemo(
    () => new Set(collectionQuery.data?.map((entry) => entry.pokemonId)),
    [collectionQuery.data],
  );

  const addMutation = useMutation({
    mutationFn: collectionApi.add,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['collection'] }),
  });

  const removeMutation = useMutation({
    mutationFn: collectionApi.remove,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['collection'] }),
  });

  const busyId = addMutation.isPending
    ? addMutation.variables
    : removeMutation.isPending
      ? removeMutation.variables
      : undefined;

  return (
    <div className="page">
      <div className="page-header">
        <h1>Browse Pokémon</h1>
        <input
          className="search-input"
          type="search"
          placeholder="Search by name…"
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
        />
      </div>

      {pokemonQuery.isLoading && <p>Loading Pokémon…</p>}
      {pokemonQuery.isError && <p className="form-error">Could not load Pokémon from the catalog.</p>}

      {pokemonQuery.data && (
        <>
          <div className="pokemon-grid">
            {pokemonQuery.data.content.map((pokemon) => (
              <PokemonCard
                key={pokemon.id}
                pokemon={pokemon}
                owned={ownedIds.has(pokemon.id)}
                onAdd={(id) => addMutation.mutate(id)}
                onRemove={(id) => removeMutation.mutate(id)}
                busy={busyId === pokemon.id}
              />
            ))}
          </div>

          {pokemonQuery.data.content.length === 0 && <p>No Pokémon match your search.</p>}

          <div className="pagination">
            <button disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
              Previous
            </button>
            <span>
              Page {pokemonQuery.data.page + 1} of {Math.max(pokemonQuery.data.totalPages, 1)}
            </span>
            <button
              disabled={page + 1 >= pokemonQuery.data.totalPages}
              onClick={() => setPage((p) => p + 1)}
            >
              Next
            </button>
          </div>
        </>
      )}
    </div>
  );
}
