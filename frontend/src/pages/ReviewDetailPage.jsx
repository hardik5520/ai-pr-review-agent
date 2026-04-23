import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { apiFetch } from '../api/client';

/**
 * Review Detail page — shows the full AI-generated review for one PR.
 *
 * Flow:
 *   1. Read the review ID from the URL (e.g. /review/3 → id = "3")
 *   2. Fetch GET /api/reviews/:id
 *   3. Render the full review summary + each parsed comment
 */
export default function ReviewDetailPage() {
  const [review, setReview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // useParams reads dynamic segments from the URL.
  // Route is defined as /review/:id → useParams gives us { id: "3" }
  const { id } = useParams();

  const navigate = useNavigate();

  useEffect(() => {
    apiFetch(`/api/reviews/${id}`)
      .then(data => setReview(data))
      .catch(err => setError(err.message))
      .finally(() => setLoading(false));
  }, [id]); // re-fetch if the id in the URL changes

  // Badge color per severity
  function severityBadge(severity) {
    if (severity === 'CRITICAL') return 'bg-red-900 text-red-300 border border-red-700';
    if (severity === 'WARNING')  return 'bg-yellow-900 text-yellow-300 border border-yellow-700';
    return 'bg-blue-900 text-blue-300 border border-blue-700';
  }

  // Emoji prefix per severity
  function severityEmoji(severity) {
    if (severity === 'CRITICAL') return '🔴';
    if (severity === 'WARNING')  return '🟡';
    return '🔵';
  }

  return (
    <div className="min-h-screen bg-gray-950">
      <Navbar />

      <div className="max-w-4xl mx-auto px-6 py-8">

        {loading && <p className="text-gray-400 text-sm">Loading review...</p>}
        {error   && <p className="text-red-400 text-sm">Error: {error}</p>}

        {review && (
          <>
            {/* Back button */}
            <button
              onClick={() => navigate('/dashboard')}
              className="text-gray-400 hover:text-white text-sm mb-6 flex items-center gap-1 transition-colors"
            >
              ← Back to Dashboard
            </button>

            {/* PR header card */}
            <div className="bg-gray-900 border border-gray-800 rounded-xl p-6 mb-6">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <h1 className="text-xl font-bold text-white">{review.prTitle}</h1>
                  <p className="text-gray-400 text-sm mt-1">
                    {review.repoName} &nbsp;·&nbsp; PR #{review.prNumber} &nbsp;·&nbsp; by {review.author}
                  </p>
                </div>
                {/* Overall severity badge */}
                <span className={`px-3 py-1 rounded-full text-xs font-semibold shrink-0 ${severityBadge(review.overallSeverity)}`}>
                  {severityEmoji(review.overallSeverity)} {review.overallSeverity}
                </span>
              </div>

              {/* Meta row */}
              <div className="flex gap-6 mt-4 text-xs text-gray-500">
                <span>Model: <span className="text-gray-300">{review.modelUsed}</span></span>
                <span>Reviewed: <span className="text-gray-300">
                  {new Date(review.createdAt).toLocaleString('en-US', {
                    month: 'short', day: 'numeric', year: 'numeric',
                    hour: '2-digit', minute: '2-digit'
                  })}
                </span></span>
              </div>
            </div>

            {/* Full AI review text */}
            <div className="bg-gray-900 border border-gray-800 rounded-xl p-6 mb-6">
              <h2 className="text-white font-semibold mb-4">Full Review</h2>
              {/* whitespace-pre-wrap preserves newlines from the AI response */}
              <p className="text-gray-300 text-sm leading-relaxed whitespace-pre-wrap">
                {review.summary}
              </p>
            </div>

            {/* Parsed comments section */}
            {review.comments.length > 0 && (
              <div className="bg-gray-900 border border-gray-800 rounded-xl p-6">
                <h2 className="text-white font-semibold mb-4">
                  Comments
                  <span className="ml-2 text-xs text-gray-500 font-normal">
                    {review.comments.length} found
                  </span>
                </h2>

                <div className="flex flex-col gap-3">
                  {review.comments.map(comment => (
                    <div
                      key={comment.id}
                      className="flex items-start gap-3 bg-gray-800 rounded-lg p-4"
                    >
                      {/* Severity badge */}
                      <span className={`px-2 py-0.5 rounded text-xs font-medium shrink-0 mt-0.5 ${severityBadge(comment.severity)}`}>
                        {severityEmoji(comment.severity)} {comment.severity}
                      </span>
                      {/* Comment text */}
                      <p className="text-gray-300 text-sm leading-relaxed">
                        {comment.commentText}
                      </p>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </>
        )}

      </div>
    </div>
  );
}
