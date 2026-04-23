import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import ReviewDetailPage from './pages/ReviewDetailPage';
import TrendsPage from './pages/TrendsPage';

/**
 * App is the root component — it sets up client-side routing.
 *
 * BrowserRouter  — enables URL-based navigation (uses the browser's History API)
 * Routes         — container that holds all route definitions
 * Route          — maps a URL path to a component
 * Navigate       — programmatically redirects to another route
 *
 * How routing works:
 *   User visits /dashboard → React renders <DashboardPage /> without a full page reload
 *   URL changes but only the matched component re-renders — the rest of the app stays mounted
 */
function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Login page — public, no auth required */}
        <Route path="/" element={<LoginPage />} />

        {/* Dashboard — shows the table of all reviews */}
        <Route path="/dashboard" element={<DashboardPage />} />

        {/* Review detail — :id is a URL parameter, e.g. /review/1 */}
        <Route path="/review/:id" element={<ReviewDetailPage />} />

        {/* Trends — bar chart of PRs per author */}
        <Route path="/trends" element={<TrendsPage />} />

        {/* Catch-all — redirect any unknown URL back to login */}
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
