"use client";
import React, { useState } from 'react';
import ProtectedRoute from '@/components/ProtectedRoute';
import DashboardHeader from '@/components/dashboard/Header';
import { getReportPeriod } from '@/lib/reports/api';
import { Download } from 'lucide-react';

export default function ReportsPage() {
  const [from, setFrom] = useState<string>(() => new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().slice(0,10));
  const [to, setTo] = useState<string>(() => new Date().toISOString().slice(0,10));
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function onGenerate(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const csv = await getReportPeriod(from, to);
      // create blob and trigger download
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `emissions-report-${from}-to-${to}.csv`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <ProtectedRoute requiredRole="viewer">
      <div className="min-h-screen bg-linear-to-br from-gray-900 via-emerald-950 to-gray-900">
        <DashboardHeader />
        <main className="max-w-4xl mx-auto px-6 py-8">
          <h1 className="text-2xl font-semibold text-gray-100 mb-4">Reports</h1>

          <div className="bg-gray-800/30 p-6 rounded-md max-w-2xl">
            <h2 className="text-lg font-medium text-white mb-2">Generate Emissions CSV</h2>
            <p className="text-sm text-gray-400 mb-4">Pick a date range and download a CSV containing emissions events.</p>

            <form onSubmit={onGenerate} className="grid grid-cols-1 md:grid-cols-2 gap-4 items-end">
              <div>
                <label className="block text-sm text-gray-300 mb-1">From</label>
                <input type="date" value={from} onChange={(e) => setFrom(e.target.value)} className="w-full px-3 py-2 bg-gray-900 border border-gray-700 rounded text-gray-100 focus:ring-2 focus:ring-emerald-400" />
              </div>
              <div>
                <label className="block text-sm text-gray-300 mb-1">To</label>
                <input type="date" value={to} onChange={(e) => setTo(e.target.value)} className="w-full px-3 py-2 bg-gray-900 border border-gray-700 rounded text-gray-100 focus:ring-2 focus:ring-emerald-400" />
              </div>

              <div className="md:col-span-2">
                <div className="flex items-center gap-3">
                  <button type="submit" disabled={loading} className="inline-flex items-center gap-2 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-md">
                    <Download className="w-4 h-4" />
                    {loading ? 'Generating…' : 'Download CSV'}
                  </button>
                  <div className="text-sm text-gray-400">CSV includes emission_id, event_id, occurred_at, category, unit, value_numeric, scope, co2e_kg</div>
                </div>
                {error && <div className="mt-3 text-red-400">{error}</div>}
              </div>
            </form>
          </div>
        </main>
      </div>
    </ProtectedRoute>
  );
}
