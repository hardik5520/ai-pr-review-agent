import { useEffect, useState } from 'react';
import Navbar from '../components/Navbar';
import { apiFetch } from '../api/client';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer
} from 'recharts';

/**
 * Trends page — shows a bar chart of PRs opened per contributor.
 *
 * Flow:
 *   1. Fetch GET /api/trends → [{ author: "hardik", prCount: 6 }, ...]
 *   2. Pass the array directly to Recharts BarChart
 *   3. Recharts reads `author` for the X axis and `prCount` for the bar height
 */
export default function TrendsPage() {
  const [trends, setTrends] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    apiFetch('/api/trends')
      .then(data => setTrends(data))
      .catch(err => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="min-h-screen bg-gray-950">
      <Navbar />

      <div className="max-w-4xl mx-auto px-6 py-8">

        {/* Page header */}
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-white">Trends</h1>
          <p className="text-gray-400 text-sm mt-1">Pull requests reviewed per contributor</p>
        </div>

        {loading && <p className="text-gray-400 text-sm">Loading trends...</p>}
        {error   && <p className="text-red-400 text-sm">Error: {error}</p>}

        {!loading && !error && (
          <div className="bg-gray-900 border border-gray-800 rounded-xl p-6">

            {trends.length === 0 ? (
              <p className="text-gray-500 text-sm text-center py-8">
                No data yet. Open a pull request to get started.
              </p>
            ) : (
              <>
                {/*
                  ResponsiveContainer makes the chart fill its parent width.
                  Without it you'd have to hardcode a pixel width.
                  height={350} sets a fixed height in pixels.
                */}
                <ResponsiveContainer width="100%" height={350}>
                  <BarChart
                    data={trends}
                    margin={{ top: 10, right: 20, left: 0, bottom: 5 }}
                  >
                    {/*
                      CartesianGrid draws the faint grid lines behind the bars.
                      strokeDasharray="3 3" makes them dashed.
                    */}
                    <CartesianGrid strokeDasharray="3 3" stroke="#374151" />

                    {/*
                      XAxis reads the `author` field from each data object for labels.
                      dataKey tells Recharts which field to use on this axis.
                    */}
                    <XAxis
                      dataKey="author"
                      tick={{ fill: '#9ca3af', fontSize: 13 }}
                      axisLine={{ stroke: '#374151' }}
                      tickLine={false}
                    />

                    {/*
                      YAxis shows the count scale. allowDecimals=false keeps it whole numbers.
                    */}
                    <YAxis
                      allowDecimals={false}
                      tick={{ fill: '#9ca3af', fontSize: 13 }}
                      axisLine={{ stroke: '#374151' }}
                      tickLine={false}
                    />

                    {/*
                      Tooltip shows a popup on hover with the exact value.
                      Custom styles to match the dark theme.
                    */}
                    <Tooltip
                      contentStyle={{
                        backgroundColor: '#1f2937',
                        border: '1px solid #374151',
                        borderRadius: '8px',
                        color: '#f3f4f6',
                      }}
                      cursor={{ fill: 'rgba(255,255,255,0.05)' }}
                    />

                    {/*
                      Bar is the actual bar.
                      dataKey="prCount" tells it which field determines bar height.
                      fill sets the bar colour.
                      radius rounds the top corners of each bar.
                    */}
                    <Bar dataKey="prCount" fill="#3b82f6" radius={[4, 4, 0, 0]} />

                  </BarChart>
                </ResponsiveContainer>

                {/* Summary table below the chart */}
                <div className="mt-6 border-t border-gray-800 pt-6">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="text-gray-500 text-xs uppercase tracking-wider">
                        <th className="text-left pb-3">Author</th>
                        <th className="text-right pb-3">PR Count</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-800">
                      {trends.map(row => (
                        <tr key={row.author}>
                          <td className="py-3 text-gray-300">{row.author}</td>
                          <td className="py-3 text-right text-blue-400 font-medium">{row.prCount}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </>
            )}

          </div>
        )}

      </div>
    </div>
  );
}
