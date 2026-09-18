import { httpClient } from './httpClient';
import type { CollectionEntry } from './types';

export const collectionApi = {
  list: () => httpClient.get<CollectionEntry[]>('/collection').then((res) => res.data),

  add: (pokemonId: number) =>
    httpClient.post<CollectionEntry>(`/collection/${pokemonId}`).then((res) => res.data),

  remove: (pokemonId: number) => httpClient.delete(`/collection/${pokemonId}`).then(() => undefined),
};
