import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

/**
 * Login page — the entry point of the app.
 *
 * Flow:
 *   1. User types username + password
 *   2. On submit → POST /auth/login with credentials
 *   3. Server returns { token: "eyJ..." }
 *   4. We store the token in localStorage
 *   5. Redirect to /dashboard
 *
 * We don't use apiFetch here because /auth/login is public —
 * there's no token yet to attach. We use plain fetch directly.
 */
export default function LoginPage() {
  // Controlled inputs — React owns the input values, not the DOM
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  // Error message to show below the form on failed login
  const [error, setError] = useState('');

  // Loading state — disables the button while the request is in flight
  const [loading, setLoading] = useState(false);

  // useNavigate gives us a function to programmatically change the URL
  const navigate = useNavigate();

  async function handleSubmit(e) {
    // Prevent the default browser form submission (which would reload the page)
    e.preventDefault();

    setLoading(true);
    setError(''); // clear any previous error

    try {
      const response = await fetch('http://localhost:8080/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      });

      if (!response.ok) {
        // 401 Unauthorized — wrong credentials
        setError('Invalid username or password');
        return;
      }

      const data = await response.json(); // { token: "eyJ..." }

      // Store the JWT in localStorage so apiFetch can read it on every future request
      // localStorage persists across page refreshes until explicitly cleared
      localStorage.setItem('token', data.token);

      // Redirect to the dashboard
      navigate('/dashboard');
    } catch (err) {
      // Network error — backend not running, CORS issue etc.
      setError('Could not connect to server. Is the backend running?');
    } finally {
      // Always re-enable the button whether the request succeeded or failed
      setLoading(false);
    }
  }

  return (
    // Full-screen dark background, centered content
    <div className="min-h-screen bg-gray-950 flex items-center justify-center">

      {/* Login card */}
      <div className="bg-gray-900 border border-gray-800 rounded-2xl p-8 w-full max-w-sm shadow-xl">

        {/* Header */}
        <div className="mb-8 text-center">
          <h1 className="text-2xl font-bold text-white">PR Review Agent</h1>
          <p className="text-gray-400 text-sm mt-1">Sign in to your dashboard</p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">

          {/* Username field */}
          <div className="flex flex-col gap-1">
            <label className="text-sm text-gray-400">Username</label>
            <input
              type="text"
              value={username}
              onChange={e => setUsername(e.target.value)}
              required
              placeholder="admin"
              className="bg-gray-800 text-white border border-gray-700 rounded-lg px-4 py-2 text-sm focus:outline-none focus:border-blue-500"
            />
          </div>

          {/* Password field */}
          <div className="flex flex-col gap-1">
            <label className="text-sm text-gray-400">Password</label>
            <input
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
              placeholder="••••••••"
              className="bg-gray-800 text-white border border-gray-700 rounded-lg px-4 py-2 text-sm focus:outline-none focus:border-blue-500"
            />
          </div>

          {/* Error message — only shown when error state is set */}
          {error && (
            <p className="text-red-400 text-sm text-center">{error}</p>
          )}

          {/* Submit button */}
          <button
            type="submit"
            disabled={loading}
            className="bg-blue-600 hover:bg-blue-700 disabled:bg-blue-900 disabled:cursor-not-allowed text-white font-medium py-2 rounded-lg text-sm transition-colors mt-2"
          >
            {loading ? 'Signing in...' : 'Sign in'}
          </button>

        </form>
      </div>
    </div>
  );
}
