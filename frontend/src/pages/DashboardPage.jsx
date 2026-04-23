import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { apiFetch } from '../api/client';

/**
 * Dashboard page — shows a table of all AI-generated reviews.
 *
 * Flow:
 *   1. Component mounts → useEffect fires → calls GET /api/reviews
 *   2. Reviews saved to state → table renders
 *   3. Clicking a row navigates to /review/:id
 */
export default function DashboardPage() {
  // List of reviews fetched from the backend
  const [reviews, setReviews] = useState([]);

  // Loading and error states for UX feedback
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const navigate = useNavigate();

  // useEffect runs after the component first renders.
  // The empty [] dependency array means "run once on mount" — like componentDidMount.
  // This is where you trigger data fetching in React.
  useEffect(() => {
    apiFetch('/api/reviews')
      .then(data => setReviews(data))
      .catch(err => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  // Helper — returns a Tailwind color class based on severity string
  function severityColor(severity) {
    if (severity === 'CRITICAL') return 'text-red-400';
    if (severity === 'WARNING')  return 'text-yellow-400';
    return 'text-blue-400'; // INFO
  }

  // Helper — returns a badge style based on severity
  function severityBadge(severity) {
    if (severity === 'CRITICAL') return 'bg-red-900 text-red-300';
    if (severity === 'WARNING')  return 'bg-yellow-900 text-yellow-300';
    return 'bg-blue-900 text-blue-300'; // INFO
  }

  return (
    <div className="min-h-screen bg-gray-950">
      <Navbar />

      <div className="max-w-6xl mx-auto px-6 py-8">

        {/* Page header */}
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-white">Reviews</h1>
          <p className="text-gray-400 text-sm mt-1">All AI-generated pull request reviews</p>
        </div>

        {/* Loading state */}
        {loading && (
          <p className="text-gray-400 text-sm">Loading reviews...</p>
        )}

        {/* Error state */}
        {error && (
          <p className="text-red-400 text-sm">Error: {error}</p>
        )}

        {/* Reviews table — only shown when data is ready */}
        {!loading && !error && (
          <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden">
            <table className="w-full text-sm">

              {/* Table header */}
              <thead className="bg-gray-800 text-gray-400 uppercase text-xs tracking-wider">
                <tr>
                  <th className="px-5 py-3 text-left">Repository</th>
                  <th className="px-5 py-3 text-left">PR</th>
                  <th className="px-5 py-3 text-left">Author</th>
                  <th className="px-5 py-3 text-left">Severity</th>
                  <th className="px-5 py-3 text-left">Model</th>
                  <th className="px-5 py-3 text-left">Date</th>
                </tr>
              </thead>

              {/* Table body */}
              <tbody className="divide-y divide-gray-800">
                {reviews.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="px-5 py-8 text-center text-gray-500">
                      No reviews yet. Open a pull request to trigger the agent.
                    </td>
                  </tr>
                ) : (
                  reviews.map(review => (
                    // Clicking a row navigates to the detail page for that review
                    <tr
                      key={review.id}
                      onClick={() => navigate(`/review/${review.id}`)}
                      className="hover:bg-gray-800 cursor-pointer transition-colors"
                    >
                      <td className="px-5 py-4 text-gray-300 font-mono text-xs">
                        {review.repoName}
                      </td>
                      <td className="px-5 py-4">
                        <div className="text-white font-medium">#{review.prNumber}</div>
                        <div className="text-gray-400 text-xs truncate max-w-[180px]">
                          {review.prTitle}
                        </div>
                      </td>
                      <td className="px-5 py-4 text-gray-300">{review.author}</td>
                      <td className="px-5 py-4">
                        {/* Severity badge */}
                        <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${severityBadge(review.overallSeverity)}`}>
                          {review.overallSeverity}
                        </span>
                      </td>
                      <td className="px-5 py-4 text-gray-400">{review.modelUsed}</td>
                      <td className="px-5 py-4 text-gray-400 text-xs">
                        {/* Format ISO date string to readable format */}
                        {new Date(review.createdAt).toLocaleDateString('en-US', {
                          month: 'short', day: 'numeric', year: 'numeric'
                        })}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>

            </table>
          </div>
        )}

      </div>
    </div>
  );
}
