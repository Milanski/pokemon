import axios from 'axios';
import type { ProblemDetail } from './types';

export const TOKEN_STORAGE_KEY = 'pokemon-collection.token';

export const httpClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
});

httpClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/** Fired when the backend rejects a token, so the app can log the trainer out. */
export const AUTH_EXPIRED_EVENT = 'pokemon-collection.auth-expired';

httpClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
      window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT));
    }
    return Promise.reject(error);
  },
);

export function extractErrorMessage(error: unknown, fallback: string): string {
  if (axios.isAxiosError<ProblemDetail>(error) && error.response?.data?.detail) {
    return error.response.data.detail;
  }
  return fallback;
}
