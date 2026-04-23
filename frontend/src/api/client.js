// Base URL of the Spring Boot backend
const BASE_URL = 'http://localhost:8080';

/**
 * Central fetch wrapper for all API calls.
 *
 * Why not use fetch() directly in each component?
 * Because every request needs the same two things:
 *   1. The base URL prepended
 *   2. The JWT token in the Authorization header
 *
 * Putting this logic here means we write it once and every page just calls apiFetch('/api/reviews').
 *
 * @param {string} path    - API path e.g. '/api/reviews' or '/api/reviews/1'
 * @param {object} options - Optional fetch options (method, body, extra headers)
 * @returns {Promise<any>} - Parsed JSON response
 * @throws {Error}         - If the response status is not 2xx
 */
export async function apiFetch(path, options = {}) {
  // Read the JWT token stored in localStorage after login
  const token = localStorage.getItem('token');

  const response = await fetch(`${BASE_URL}${path}`, {
    ...options, // spread any extra options (method, body etc.) passed by the caller
    headers: {
      'Content-Type': 'application/json',
      // If a token exists, attach it as a Bearer token
      // Every protected endpoint (/api/*) requires this header
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers, // allow caller to override headers if needed
    },
  });

  // fetch() does NOT throw on 4xx/5xx — we must check response.ok manually
  // response.ok is true for status codes 200-299
  if (!response.ok) {
    // If the server returned 401, the token is invalid or expired — redirect to login
    if (response.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/';
    }
    throw new Error(`Request failed: ${response.status} ${response.statusText}`);
  }

  // Parse and return the JSON body
  return response.json();
}
