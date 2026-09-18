import { Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import { RequireAuth } from './auth/RequireAuth';
import { LoginPage } from './auth/LoginPage';
import { RegisterPage } from './auth/RegisterPage';
import { AppLayout } from './layout/AppLayout';
import { PokemonListPage } from './pokemon/PokemonListPage';
import { MyCollectionPage } from './collection/MyCollectionPage';

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          element={
            <RequireAuth>
              <AppLayout />
            </RequireAuth>
          }
        >
          <Route path="/pokemon" element={<PokemonListPage />} />
          <Route path="/collection" element={<MyCollectionPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/pokemon" replace />} />
      </Routes>
    </AuthProvider>
  );
}
