import { httpClient } from './httpClient';
import type { PageResponse, PokemonDetails, PokemonSummary } from './types';

export const pokemonApi = {
  list: (page: number, size: number, search: string) =>
    httpClient
      .get<PageResponse<PokemonSummary>>('/pokemon', { params: { page, size, search: search || undefined } })
      .then((res) => res.data),

  getById: (id: number) => httpClient.get<PokemonDetails>(`/pokemon/${id}`).then((res) => res.data),
};
