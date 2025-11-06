"use client";
import React, { useState } from "react";
import ProtectedRoute from '@/components/ProtectedRoute';
import DashboardHeader from '@/components/dashboard/Header';
import { ingestEvents, uploadCsv } from "@/lib/ingest/api";
import type { IngestResponse } from "@/types/ingest/ingesttypes";
import { Upload, FileText, Send, AlertCircle, CheckCircle } from 'lucide-react';

export default function IngestPage() {
  const [jsonText, setJsonText] = useState(`{
  "events": [
    {
      "occurred_at": "2024-01-15T10:00:00Z",
      "category": "electricity",
      "unit": "kWh",
      "value_numeric": 100,
      "facility_id": 1,
      "source_id": "meter_001"
    }
  ]
}`);
  const [result, setResult] = useState<IngestResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submitJson(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setResult(null);
    setLoading(true);
    try {
      const payload = JSON.parse(jsonText);
      const res = await ingestEvents(payload);
      setResult(res);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
    }
  }

  async function onFile(e: React.ChangeEvent<HTMLInputElement>) {
    setError(null);
    setResult(null);
    const file = e.target.files?.[0];
    if (!file) return;
    setLoading(true);
    try {
      const res = await uploadCsv(file);
      setResult(res);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <ProtectedRoute>
      <div>
        <DashboardHeader />
        <div className="min-h-screen bg-linear-to-br from-gray-900 via-emerald-950 to-gray-900 p-8">
          <div className="max-w-6xl mx-auto">
          <div className="mb-8">
            <h1 className="text-4xl font-bold text-white mb-2">Data Ingestion</h1>
            <p className="text-gray-400">Upload emissions data via JSON API or CSV file</p>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* JSON Input */}
            <div className="bg-gray-800/40 backdrop-blur-sm rounded-xl p-6 border border-gray-700/50">
              <div className="flex items-center gap-3 mb-6">
                <div className="p-2 bg-emerald-500/20 rounded-lg">
                  <FileText className="w-6 h-6 text-emerald-400" />
                </div>
                <div>
                  <h2 className="text-xl font-semibold text-white">JSON API</h2>
                  <p className="text-gray-400 text-sm">Send events directly via JSON</p>
                </div>
              </div>

              <form onSubmit={submitJson} className="space-y-6">
                <div>
                  <label className="block text-white font-medium mb-3">Events JSON</label>
                  <textarea
                    value={jsonText}
                    onChange={(e) => setJsonText(e.target.value)}
                    rows={12}
                    className="w-full px-4 py-3 bg-gray-700/50 border border-gray-600 rounded-lg text-white font-mono text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                    placeholder="Enter your events JSON here..."
                  />
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="w-full flex items-center justify-center gap-2 px-6 py-3 bg-emerald-500 hover:bg-emerald-600 text-gray-900 font-semibold rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {loading ? (
                    <>
                      <div className="w-5 h-5 border-2 border-gray-900 border-t-transparent rounded-full animate-spin"></div>
                      Processing...
                    </>
                  ) : (
                    <>
                      <Send className="w-5 h-5" />
                      Send JSON
                    </>
                  )}
                </button>
              </form>
            </div>

            {/* CSV Upload */}
            <div className="bg-gray-800/40 backdrop-blur-sm rounded-xl p-6 border border-gray-700/50">
              <div className="flex items-center gap-3 mb-6">
                <div className="p-2 bg-blue-500/20 rounded-lg">
                  <Upload className="w-6 h-6 text-blue-400" />
                </div>
                <div>
                  <h2 className="text-xl font-semibold text-white">CSV Upload</h2>
                  <p className="text-gray-400 text-sm">Upload bulk data from CSV file</p>
                </div>
              </div>

              <div className="space-y-6">
                <div>
                  <label className="block text-white font-medium mb-3">Upload CSV File</label>
                  <div className="relative">
                    <input
                      type="file"
                      accept="text/csv,.csv"
                      onChange={onFile}
                      disabled={loading}
                      className="w-full px-4 py-3 bg-gray-700/50 border border-gray-600 rounded-lg text-white file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:bg-emerald-500 file:text-gray-900 file:font-medium hover:file:bg-emerald-600 transition-colors disabled:opacity-50"
                    />
                  </div>
                  <p className="mt-2 text-sm text-gray-400">
                    CSV should include: occurred_at, category, unit, value_numeric, facility_id
                  </p>
                </div>

                <div className="bg-gray-700/30 rounded-lg p-4">
                  <h3 className="text-white font-medium mb-2">Expected CSV Format:</h3>
                  <code className="text-gray-300 text-xs">
                    occurred_at,category,unit,value_numeric,facility_id<br/>
                    2024-01-15T10:00:00Z,electricity,kWh,100,1
                  </code>
                </div>
              </div>
            </div>
          </div>

          {/* Results */}
          {(result || error) && (
            <div className="mt-8">
              {error && (
                <div className="bg-red-900/50 border border-red-700 rounded-xl p-6 mb-6">
                  <div className="flex items-center gap-3">
                    <AlertCircle className="w-6 h-6 text-red-400 shrink-0" />
                    <div>
                      <h3 className="text-red-400 font-semibold mb-1">Error</h3>
                      <p className="text-gray-300 text-sm">{error}</p>
                    </div>
                  </div>
                </div>
              )}

              {result && (
                <div className="bg-emerald-900/50 border border-emerald-700 rounded-xl p-6">
                  <div className="flex items-center gap-3 mb-4">
                    <CheckCircle className="w-6 h-6 text-emerald-400 shrink-0" />
                    <h3 className="text-emerald-400 font-semibold">Ingestion Successful</h3>
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div className="bg-gray-800/50 rounded-lg p-4">
                      <div className="text-2xl font-bold text-white">{result.created_events}</div>
                      <div className="text-gray-400 text-sm">Events Created</div>
                    </div>
                    <div className="bg-gray-800/50 rounded-lg p-4">
                      <div className="text-2xl font-bold text-yellow-400">{result.skipped_duplicates}</div>
                      <div className="text-gray-400 text-sm">Duplicates Skipped</div>
                    </div>
                    <div className="bg-gray-800/50 rounded-lg p-4">
                      <div className="text-2xl font-bold text-blue-400">{result.created_emissions}</div>
                      <div className="text-gray-400 text-sm">Emissions Calculated</div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
        </div>
      </div>
    </ProtectedRoute>
  );
}
