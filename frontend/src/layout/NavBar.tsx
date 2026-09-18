import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export function NavBar() {
  const { username, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  return (
    <nav className="nav-bar">
      <span className="nav-brand">Pokémon Collection</span>
      <div className="nav-links">
        <NavLink to="/pokemon" className={({ isActive }) => (isActive ? 'active' : '')}>
          Browse
        </NavLink>
        <NavLink to="/collection" className={({ isActive }) => (isActive ? 'active' : '')}>
          My Collection
        </NavLink>
      </div>
      <div className="nav-user">
        <span>{username}</span>
        <button onClick={handleLogout}>Log out</button>
      </div>
    </nav>
  );
}
