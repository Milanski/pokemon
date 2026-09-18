import { httpClient } from './httpClient';
import type { AuthResponse } from './types';

export interface RegisterPayload {
  username: string;
  email: string;
  password: string;
}

export interface LoginPayload {
  username: string;
  password: string;
}

export const authApi = {
  register: (payload: RegisterPayload) =>
    httpClient.post<AuthResponse>('/auth/register', payload).then((res) => res.data),

  login: (payload: LoginPayload) =>
    httpClient.post<AuthResponse>('/auth/login', payload).then((res) => res.data),
};
