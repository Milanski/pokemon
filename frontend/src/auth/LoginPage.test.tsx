import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { LoginPage } from './LoginPage';
import { AuthProvider } from './AuthContext';
import { authApi } from '../api/authApi';

vi.mock('../api/authApi', () => ({
  authApi: {
    login: vi.fn(),
    register: vi.fn(),
  },
}));

function renderLoginPage() {
  return render(
    <MemoryRouter initialEntries={['/login']}>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/pokemon" element={<div>Pokemon page</div>} />
        </Routes>
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe('LoginPage', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.mocked(authApi.login).mockReset();
  });

  it('logs in and navigates to the pokemon page on success', async () => {
    vi.mocked(authApi.login).mockResolvedValue({ trainerId: 't1', username: 'ash', token: 'jwt' });

    renderLoginPage();

    await userEvent.type(screen.getByLabelText(/username/i), 'ash');
    await userEvent.type(screen.getByLabelText(/password/i), 'trainerpw1');
    await userEvent.click(screen.getByRole('button', { name: /log in/i }));

    expect(await screen.findByText('Pokemon page')).toBeInTheDocument();
    expect(authApi.login).toHaveBeenCalledWith({ username: 'ash', password: 'trainerpw1' });
    expect(localStorage.getItem('pokemon-collection.token')).toBe('jwt');
  });

  it('shows an error message when login fails', async () => {
    vi.mocked(authApi.login).mockRejectedValue({
      isAxiosError: true,
      response: { data: { detail: 'username or password is incorrect' } },
    });

    renderLoginPage();

    await userEvent.type(screen.getByLabelText(/username/i), 'ash');
    await userEvent.type(screen.getByLabelText(/password/i), 'wrong');
    await userEvent.click(screen.getByRole('button', { name: /log in/i }));

    expect(await screen.findByText(/incorrect/i)).toBeInTheDocument();
  });
});
