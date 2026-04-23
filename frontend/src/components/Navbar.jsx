import { Link, useNavigate } from 'react-router-dom';

/**
 * Navbar — shown on every page after login.
 *
 * Contains:
 *   - App title (links back to dashboard)
 *   - Navigation links: Dashboard, Trends
 *   - Logout button (clears JWT from localStorage and redirects to login)
 *
 * Link from react-router-dom renders an <a> tag but navigates
 * client-side — no full page reload.
 */
export default function Navbar() {
  const navigate = useNavigate();

  function handleLogout() {
    // Remove the JWT — any future apiFetch call will have no token
    // and the backend will return 401, but we redirect before that happens
    localStorage.removeItem('token');
    navigate('/');
  }

  return (
    <nav className="bg-gray-900 border-b border-gray-800 px-6 py-4 flex items-center justify-between">

      {/* Left side — app title */}
      <Link to="/dashboard" className="text-white font-bold text-lg tracking-tight">
        🤖 PR Review Agent
      </Link>

      {/* Right side — nav links + logout */}
      <div className="flex items-center gap-6">
        <Link
          to="/dashboard"
          className="text-gray-400 hover:text-white text-sm transition-colors"
        >
          Dashboard
        </Link>
        <Link
          to="/trends"
          className="text-gray-400 hover:text-white text-sm transition-colors"
        >
          Trends
        </Link>
        <button
          onClick={handleLogout}
          className="text-sm bg-gray-800 hover:bg-gray-700 text-gray-300 px-3 py-1.5 rounded-lg transition-colors"
        >
          Logout
        </button>
      </div>

    </nav>
  );
}
