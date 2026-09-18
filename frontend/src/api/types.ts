export interface AuthResponse {
  trainerId: string;
  username: string;
  token: string;
}

export interface PokemonSummary {
  id: number;
  name: string;
  spriteUrl: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface PokemonDetails {
  id: number;
  name: string;
  spriteUrl: string;
  types: string[];
  heightDecimetres: number;
  weightHectograms: number;
  baseExperience: number;
  stats: Record<string, number>;
}

export interface CollectionEntry {
  pokemonId: number;
  pokemonName: string;
  spriteUrl: string;
  caughtAt: string;
}

/** RFC 7807 (application/problem+json) error body returned by the backend. */
export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail?: string;
  instance?: string;
  timestamp?: string;
}
