"use client";
import React, { useEffect, useState } from "react";
import ProtectedRoute from "@/components/ProtectedRoute";
import DashboardHeader from '@/components/dashboard/Header';
import { getKpis, getTrend, getSummary } from "@/lib/analytics/api";
import type { KpisResponse, TrendPoint, SummaryResponse } from "@/types/analytics/analyticstypes";
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer,
  LineChart,
  Line,
  PieChart,
  Pie,
  Cell
} from 'recharts';
import { 
  TrendingUp, 
  TrendingDown, 
  Calendar,
  Factory,
  Leaf,
  Activity,
  AlertCircle,
  RefreshCw
} from 'lucide-react';

const SCOPE_COLORS = {
  scope1: '#ef4444', // red-500
  scope2: '#f97316', // orange-500  
  scope3: '#eab308', // yellow-500
};

const CHART_COLORS = ['#10b981', '#3b82f6', '#8b5cf6', '#f59e0b', '#ef4444'];

export default function AnalyticsPage() {
  const [kpis, setKpis] = useState<KpisResponse | null>(null);
  const [trend, setTrend] = useState<TrendPoint[]>([]);
  const [summary, setSummary] = useState<SummaryResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [dateRange, setDateRange] = useState({
    from: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], // 30 days ago
    to: new Date().toISOString().split('T')[0] // today
  });

  async function loadAnalytics() {
    setLoading(true);
    setError(null);
    try {
      const [k, t, s] = await Promise.all([
        getKpis(dateRange.from, dateRange.to),
        getTrend(dateRange.from, dateRange.to, 'day'),
        getSummary()
      ]);
      setKpis(k);
      setTrend(t);
      setSummary(s);
    } catch (err) {
      console.error('Analytics error:', err);
      setError(err instanceof Error ? err.message : 'Failed to load analytics data');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadAnalytics();
  }, [dateRange.from, dateRange.to]);

  const formatCO2 = (value: number) => {
    if (value >= 1000) {
      return `${(value / 1000).toFixed(1)}t`;
    }
    return `${value.toFixed(1)}kg`;
  };

  const scopeChartData = kpis ? [
    { name: 'Scope 1', value: kpis.scope1_kg, color: SCOPE_COLORS.scope1 },
    { name: 'Scope 2', value: kpis.scope2_kg, color: SCOPE_COLORS.scope2 },
    { name: 'Scope 3', value: kpis.scope3_kg, color: SCOPE_COLORS.scope3 },
  ].filter(item => item.value > 0) : [];

  const topCategoriesData = summary?.top_categories?.map(([category, value], index) => ({
    category: String(category).replace('_', ' ').toUpperCase(),
    value,
    color: CHART_COLORS[index % CHART_COLORS.length]
  })) || [];

  return (
    <ProtectedRoute requiredRole="viewer">
      <React.Fragment>
        <DashboardHeader />
        <div className="min-h-screen bg-linear-to-br from-gray-900 via-gray-800 to-emerald-900">
          <div className="container mx-auto px-6 py-8">
          {/* Header */}
          <div className="flex items-center justify-between mb-8">
            <div>
              <h1 className="text-4xl font-bold text-white mb-2">Analytics Dashboard</h1>
              <p className="text-gray-400">Detailed emissions analytics and insights</p>
            </div>
            <button
              onClick={loadAnalytics}
              disabled={loading}
              className="flex items-center gap-2 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 disabled:bg-gray-600 text-white rounded-lg transition-colors"
            >
              <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
              Refresh
            </button>
          </div>

          {/* Date Range Selector */}
          <div className="bg-gray-800/50 backdrop-blur-sm rounded-xl p-6 border border-gray-700/50 mb-8">
            <div className="flex items-center gap-4">
              <Calendar className="w-5 h-5 text-emerald-400" />
              <span className="text-white font-medium">Date Range:</span>
              <input
                type="date"
                value={dateRange.from}
                onChange={(e) => setDateRange(prev => ({ ...prev, from: e.target.value }))}
                className="px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
                style={{ colorScheme: 'dark' }}
              />
              <span className="text-gray-400">to</span>
              <input
                type="date"
                value={dateRange.to}
                onChange={(e) => setDateRange(prev => ({ ...prev, to: e.target.value }))}
                className="px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
                style={{ colorScheme: 'dark' }}
              />
            </div>
          </div>

          {error && (
            <div className="bg-red-900/30 border border-red-700/50 rounded-lg p-4 flex items-center gap-3 mb-8">
              <AlertCircle className="w-5 h-5 text-red-400 shrink-0" />
              <p className="text-red-300">{error}</p>
            </div>
          )}

          {loading ? (
            <div className="flex items-center justify-center py-20">
              <div className="text-center">
                <RefreshCw className="w-8 h-8 text-emerald-400 animate-spin mx-auto mb-4" />
                <p className="text-gray-400">Loading analytics data...</p>
              </div>
            </div>
          ) : (
            <div className="space-y-8">
              {/* KPI Cards */}
              {kpis && (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                  <div className="bg-gray-800/50 backdrop-blur-sm rounded-xl p-6 border border-gray-700/50">
                    <div className="flex items-center justify-between mb-4">
                      <Leaf className="w-8 h-8 text-emerald-400" />
                      <span className="text-sm text-gray-400">TOTAL</span>
                    </div>
                    <div className="text-2xl font-bold text-white mb-1">
                      {formatCO2(kpis.total_co2e_kg)}
                    </div>
                    <p className="text-gray-400 text-sm">CO₂ Equivalent</p>
                  </div>

                  <div className="bg-gray-800/50 backdrop-blur-sm rounded-xl p-6 border border-gray-700/50">
                    <div className="flex items-center justify-between mb-4">
                      <div className="w-4 h-4 bg-red-500 rounded"></div>
                      <span className="text-sm text-gray-400">SCOPE 1</span>
                    </div>
                    <div className="text-2xl font-bold text-white mb-1">
                      {formatCO2(kpis.scope1_kg)}
                    </div>
                    <p className="text-gray-400 text-sm">Direct Emissions</p>
                  </div>

                  <div className="bg-gray-800/50 backdrop-blur-sm rounded-xl p-6 border border-gray-700/50">
                    <div className="flex items-center justify-between mb-4">
                      <div className="w-4 h-4 bg-orange-500 rounded"></div>
                      <span className="text-sm text-gray-400">SCOPE 2</span>
                    </div>
                    <div className="text-2xl font-bold text-white mb-1">
                      {formatCO2(kpis.scope2_kg)}
                    </div>
                    <p className="text-gray-400 text-sm">Indirect Energy</p>
                  </div>

                  <div className="bg-gray-800/50 backdrop-blur-sm rounded-xl p-6 border border-gray-700/50">
                    <div className="flex items-center justify-between mb-4">
                      <div className="w-4 h-4 bg-yellow-500 rounded"></div>
                      <span className="text-sm text-gray-400">SCOPE 3</span>
                    </div>
                    <div className="text-2xl font-bold text-white mb-1">
                      {formatCO2(kpis.scope3_kg)}
                    </div>
                    <p className="text-gray-400 text-sm">Other Indirect</p>
                  </div>
                </div>
              )}

              {/* Charts Grid */}
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* Emissions Trend */}
                {trend.length > 0 && (
                  <div className="bg-gray-800/50 backdrop-blur-sm rounded-xl p-6 border border-gray-700/50">
                    <h3 className="text-xl font-bold text-white mb-4 flex items-center gap-2">
                      <TrendingUp className="w-5 h-5 text-emerald-400" />
                      Emissions Trend
                    </h3>
                    <div className="h-80">
                      <ResponsiveContainer width="100%" height="100%">
                        <LineChart data={trend}>
                          <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                          <XAxis 
                            dataKey="period" 
                            stroke="#9ca3af"
                            fontSize={12}
                          />
                          <YAxis 
                            stroke="#9ca3af"
                            fontSize={12}
                            tickFormatter={formatCO2}
                          />
                          <Tooltip 
                            contentStyle={{ 
                              backgroundColor: '#1f2937', 
                              border: '1px solid #374151',
                              borderRadius: '8px',
                              color: '#fff'
                            }}
                            formatter={(value: number) => [formatCO2(value), 'CO₂e']}
                          />
                          <Line 
                            type="monotone" 
                            dataKey="co2e_kg" 
                            stroke="#10b981" 
                            strokeWidth={2}
                            dot={{ fill: '#10b981', strokeWidth: 2, r: 4 }}
                          />
                        </LineChart>
                      </ResponsiveContainer>
                    </div>
                  </div>
                )}

                {/* Scope Breakdown */}
                {scopeChartData.length > 0 && (
                  <div className="bg-gray-800/50 backdrop-blur-sm rounded-xl p-6 border border-gray-700/50">
                    <h3 className="text-xl font-bold text-white mb-4 flex items-center gap-2">
                      <Activity className="w-5 h-5 text-emerald-400" />
                      Scope Breakdown
                    </h3>
                    <div className="h-80">
                      <ResponsiveContainer width="100%" height="100%">
                        <PieChart>
                          <Pie
                            data={scopeChartData}
                            cx="50%"
                            cy="50%"
                            innerRadius={60}
                            outerRadius={120}
                            paddingAngle={5}
                            dataKey="value"
                          >
                            {scopeChartData.map((entry, index) => (
                              <Cell key={`cell-${index}`} fill={entry.color} />
                            ))}
                          </Pie>
                          <Tooltip 
                            contentStyle={{ 
                              backgroundColor: '#1f2937', 
                              border: '1px solid #374151',
                              borderRadius: '8px',
                              color: '#fff'
                            }}
                            formatter={(value: number) => [formatCO2(value), 'CO₂e']}
                          />
                        </PieChart>
                      </ResponsiveContainer>
                    </div>
                  </div>
                )}
              </div>

              {/* Summary and Top Categories */}
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* Summary Stats */}
                {summary && (
                  <div className="bg-gray-800/50 backdrop-blur-sm rounded-xl p-6 border border-gray-700/50">
                    <h3 className="text-xl font-bold text-white mb-6 flex items-center gap-2">
                      <Factory className="w-5 h-5 text-emerald-400" />
                      Summary Statistics
                    </h3>
                    <div className="space-y-4">
                      <div className="flex justify-between items-center py-2 border-b border-gray-700/50">
                        <span className="text-gray-400">Total Emissions</span>
                        <span className="text-white font-semibold">{formatCO2(summary.total_co2e_kg)}</span>
                      </div>
                      <div className="flex justify-between items-center py-2 border-b border-gray-700/50">
                        <span className="text-gray-400">Active Facilities</span>
                        <span className="text-white font-semibold">{summary.facilities_count}</span>
                      </div>
                      <div className="flex justify-between items-center py-2 border-b border-gray-700/50">
                        <span className="text-gray-400">Last Activity</span>
                        <span className="text-white font-semibold">
                          {summary.last_event_at 
                            ? new Date(summary.last_event_at).toLocaleDateString()
                            : 'No data'
                          }
                        </span>
                      </div>
                    </div>
                  </div>
                )}

                {/* Top Categories */}
                {topCategoriesData.length > 0 && (
                  <div className="bg-gray-800/50 backdrop-blur-sm rounded-xl p-6 border border-gray-700/50">
                    <h3 className="text-xl font-bold text-white mb-4">Top Emission Categories</h3>
                    <div className="h-80">
                      <ResponsiveContainer width="100%" height="100%">
                        <BarChart data={topCategoriesData} layout="horizontal">
                          <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                          <XAxis 
                            type="number"
                            stroke="#9ca3af"
                            fontSize={12}
                            tickFormatter={formatCO2}
                          />
                          <YAxis 
                            type="category"
                            dataKey="category"
                            stroke="#9ca3af"
                            fontSize={12}
                            width={80}
                          />
                          <Tooltip 
                            contentStyle={{ 
                              backgroundColor: '#1f2937', 
                              border: '1px solid #374151',
                              borderRadius: '8px',
                              color: '#fff'
                            }}
                            formatter={(value: number) => [formatCO2(value), 'CO₂e']}
                          />
                          <Bar dataKey="value" fill="#10b981" radius={[0, 4, 4, 0]} />
                        </BarChart>
                      </ResponsiveContainer>
                    </div>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
        </div>
      </React.Fragment>
    </ProtectedRoute>
  );
}
