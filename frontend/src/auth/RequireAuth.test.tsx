import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it } from 'vitest';
import { RequireAuth } from './RequireAuth';
import { AuthProvider } from './AuthContext';
import { TOKEN_STORAGE_KEY } from '../api/httpClient';

function renderProtectedPage() {
  return render(
    <MemoryRouter initialEntries={['/collection']}>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<div>Login page</div>} />
          <Route
            path="/collection"
            element={
              <RequireAuth>
                <div>Secret collection</div>
              </RequireAuth>
            }
          />
        </Routes>
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe('RequireAuth', () => {
  beforeEach(() => localStorage.clear());

  it('redirects to /login when there is no token', () => {
    renderProtectedPage();

    expect(screen.getByText('Login page')).toBeInTheDocument();
  });

  it('renders the protected content when a token is present', () => {
    localStorage.setItem(TOKEN_STORAGE_KEY, 'jwt');

    renderProtectedPage();

    expect(screen.getByText('Secret collection')).toBeInTheDocument();
  });
});
