"use client";
import React, { useEffect, useState } from "react";
import { listFactors, createFactor } from "@/lib/factors/api";
import DashboardHeader from '@/components/dashboard/Header';
import type { Factor, CreateFactorRequest } from "@/types/factors/factorstypes";
import { Logger } from '@/lib/logger';

export default function FactorsPage() {
  const [items, setItems] = useState<Factor[]>([]);
  const [loading, setLoading] = useState(false);
  const [category, setCategory] = useState("");
  const [unitIn, setUnitIn] = useState("");
  const [unitOut, setUnitOut] = useState("");
  const [value, setValue] = useState<number | "">("");
  const [namespace, setNamespace] = useState('global');
  const [gwpHorizon, setGwpHorizon] = useState<number>(100);
  const [geography, setGeography] = useState('GLOBAL');
  const [vendor, setVendor] = useState('unknown');
  const [method, setMethod] = useState('default');
  const [validFrom, setValidFrom] = useState<string>(() => new Date().toISOString().slice(0,10));
  const [validTo, setValidTo] = useState<string>(() => new Date(Date.now() + 365*24*60*60*1000).toISOString().slice(0,10));
  const [creating, setCreating] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  // validation state
  const [errors, setErrors] = useState<Partial<Record<string, string>>>({});
  const [error, setError] = useState<string | null>(null);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const res = await listFactors();
      setItems(res);
    } catch (err) {
      setError(String(err));
    } finally {
      setLoading(false);
    }
  }

  async function onCreate(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setSuccessMessage(null);

    // run validations
    const nextErrors: Partial<Record<string,string>> = {};
    if (!category || category.trim().length === 0) nextErrors.category = 'Category is required';
    if (!unitIn || unitIn.trim().length === 0) nextErrors.unitIn = 'Unit In is required';
    if (!unitOut || unitOut.trim().length === 0) nextErrors.unitOut = 'Unit Out is required';
    if (value === '' || Number.isNaN(Number(value))) nextErrors.value = 'Factor value is required';
    if (!vendor || vendor.trim().length === 0) nextErrors.vendor = 'Vendor is required';
    if (!method || method.trim().length === 0) nextErrors.method = 'Method is required';
    if (!validFrom) nextErrors.validFrom = 'Valid from date is required';
    if (!validTo) nextErrors.validTo = 'Valid to date is required';
    if (validFrom && validTo && new Date(validFrom) > new Date(validTo)) nextErrors.validTo = 'Valid to must be after valid from';

    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) {
      setError('Please fix validation errors');
      return;
    }
    try {
      const now = new Date();
      const payload: CreateFactorRequest = {
        namespace,
        category,
        unit_in: unitIn,
        unit_out: unitOut,
        factor_value: Number(value),
        gwp_horizon: gwpHorizon,
        geography,
        vendor,
        method,
        valid_from: new Date(validFrom).toISOString(),
        valid_to: new Date(validTo).toISOString(),
      };
      setCreating(true);
      const f = await createFactor(payload);
      setItems((s) => [f, ...s]);
      setCategory("");
      setUnitIn("");
      setUnitOut("");
      setValue("");
      setSuccessMessage(`Created factor ${f.category} (${f.id})`);
      Logger.i('FACTORS_PAGE', `Created factor ${f.id}`);
    } catch (err) {
      setError(String(err));
    } finally {
      setCreating(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  return (
    <div className="min-h-screen bg-linear-to-br from-gray-900 via-emerald-950 to-gray-900">
      <DashboardHeader />
      <main className="max-w-7xl mx-auto px-6 py-8">
        <h1 className="text-2xl font-semibold text-gray-100 mb-6">Factors</h1>

        <div className="max-w-3xl bg-gray-800/30 p-6 rounded-md">
          <form onSubmit={onCreate} className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-lg font-semibold text-white">Create factor</h2>
                <p className="text-sm text-gray-300 mt-1">Add a factor used in emissions calculations. Fields validate client-side.</p>
              </div>
              <div className="text-sm text-gray-400">Preview</div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm text-gray-300 mb-1">Category</label>
                <input placeholder="e.g. electricity" value={category} onChange={(e) => { setCategory(e.target.value); setErrors(s => ({ ...s, category: undefined })); }} className="w-full px-3 py-2 bg-gray-900 border border-gray-700 rounded-md text-gray-100 focus:outline-none focus:ring-2 focus:ring-emerald-400 transition" />
                {errors.category && <p className="text-sm text-red-400 mt-1">{errors.category}</p>}
              </div>

              <div>
                <label className="block text-sm text-gray-300 mb-1">Unit In</label>
                <input placeholder="e.g. kWh" value={unitIn} onChange={(e) => { setUnitIn(e.target.value); setErrors(s => ({ ...s, unitIn: undefined })); }} className="w-full px-3 py-2 bg-gray-900 border border-gray-700 rounded-md text-gray-100 focus:outline-none focus:ring-2 focus:ring-emerald-400 transition" />
                {errors.unitIn && <p className="text-sm text-red-400 mt-1">{errors.unitIn}</p>}
              </div>

              <div>
                <label className="block text-sm text-gray-300 mb-1">Unit Out</label>
                <input placeholder="e.g. kg CO₂e" value={unitOut} onChange={(e) => { setUnitOut(e.target.value); setErrors(s => ({ ...s, unitOut: undefined })); }} className="w-full px-3 py-2 bg-gray-900 border border-gray-700 rounded-md text-gray-100 focus:outline-none focus:ring-2 focus:ring-emerald-400 transition" />
                {errors.unitOut && <p className="text-sm text-red-400 mt-1">{errors.unitOut}</p>}
              </div>

              <div>
                <label className="block text-sm text-gray-300 mb-1">Factor Value</label>
                <input placeholder="numeric" value={value === '' ? '' : String(value)} onChange={(e) => { setValue(e.target.value === '' ? '' : Number(e.target.value)); setErrors(s => ({ ...s, value: undefined })); }} className="w-full px-3 py-2 bg-gray-900 border border-gray-700 rounded-md text-gray-100 focus:outline-none focus:ring-2 focus:ring-emerald-400 transition" />
                {errors.value && <p className="text-sm text-red-400 mt-1">{errors.value}</p>}
              </div>

              <div>
                <label className="block text-sm text-gray-300 mb-1">Namespace</label>
                <select aria-label="Namespace" value={namespace} onChange={(e) => setNamespace(e.target.value)} className="w-full px-3 py-2 bg-gray-900 border border-gray-700 rounded-md text-gray-100 focus:outline-none focus:ring-2 focus:ring-emerald-400 transition">
                  <option value="global">global</option>
                  <option value="tenant">tenant</option>
                </select>
              </div>

              <div>
                <label className="block text-sm text-gray-300 mb-1">GWP Horizon (years)</label>
                <input type="number" min={1} value={String(gwpHorizon)} onChange={(e) => { setGwpHorizon(Number(e.target.value || 0)); }} className="w-full px-3 py-2 bg-gray-900 border border-gray-700 rounded-md text-gray-100 focus:outline-none focus:ring-2 focus:ring-emerald-400 transition" aria-label="gwp horizon" />
              </div>

              <div>
                <label className="block text-sm text-gray-300 mb-1">Geography</label>
                <select aria-label="Geography" value={geography} onChange={(e) => setGeography(e.target.value)} className="w-full px-3 py-2 bg-gray-900 border border-gray-700 rounded-md text-gray-100 focus:outline-none focus:ring-2 focus:ring-emerald-400 transition">
                  <option value="GLOBAL">GLOBAL</option>
                  <option value="US">US</option>
                  <option value="EU">EU</option>
                </select>
              </div>

              <div>
                <label className="block text-sm text-gray-300 mb-1">Vendor</label>
                <input placeholder="Vendor" value={vendor} onChange={(e) => { setVendor(e.target.value); setErrors(s => ({ ...s, vendor: undefined })); }} className="w-full px-3 py-2 bg-gray-900 border border-gray-700 rounded-md text-gray-100 focus:outline-none focus:ring-2 focus:ring-emerald-400 transition" aria-label="vendor" />
                {errors.vendor && <p className="text-sm text-red-400 mt-1">{errors.vendor}</p>}
              </div>

              <div>
                <label className="block text-sm text-gray-300 mb-1">Method</label>
                <input placeholder="Method" value={method} onChange={(e) => { setMethod(e.target.value); setErrors(s => ({ ...s, method: undefined })); }} className="w-full px-3 py-2 bg-gray-900 border border-gray-700 rounded-md text-gray-100 focus:outline-none focus:ring-2 focus:ring-emerald-400 transition" aria-label="method" />
                {errors.method && <p className="text-sm text-red-400 mt-1">{errors.method}</p>}
              </div>

              <div className="md:col-span-2 grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm text-gray-300 mb-1">Valid From</label>
                  <input type="date" className="w-full px-3 py-2 bg-gray-900 border border-gray-700 rounded-md text-gray-100 focus:outline-none focus:ring-2 focus:ring-emerald-400 transition" value={validFrom} onChange={(e) => { setValidFrom(e.target.value); setErrors(s => ({ ...s, validFrom: undefined, validTo: undefined })); }} aria-label="valid from" />
                  {errors.validFrom && <p className="text-sm text-red-400 mt-1">{errors.validFrom}</p>}
                </div>
                <div>
                  <label className="block text-sm text-gray-300 mb-1">Valid To</label>
                  <input type="date" className="w-full px-3 py-2 bg-gray-900 border border-gray-700 rounded-md text-gray-100 focus:outline-none focus:ring-2 focus:ring-emerald-400 transition" value={validTo} onChange={(e) => { setValidTo(e.target.value); setErrors(s => ({ ...s, validTo: undefined })); }} aria-label="valid to" />
                  {errors.validTo && <p className="text-sm text-red-400 mt-1">{errors.validTo}</p>}
                </div>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <button type="submit" disabled={creating} className="px-4 py-2 bg-emerald-500 hover:bg-emerald-400 rounded-md text-white font-medium shadow-sm disabled:opacity-60">{creating ? 'Creating…' : 'Create Factor'}</button>
              <button type="button" onClick={() => { setCategory(''); setUnitIn(''); setUnitOut(''); setValue(''); setVendor('unknown'); setMethod('default'); setSuccessMessage(null); setErrors({}); }} className="px-4 py-2 bg-gray-700 hover:bg-gray-600 rounded-md text-gray-100">Reset</button>
            </div>
          </form>

          {error && <pre className="text-red-400 mt-4">{error}</pre>}
          {successMessage && <p className="text-green-400 mt-4">{successMessage}</p>}
          {loading ? <div className="mt-4">Loading...</div> : (
            <ul className="mt-6 space-y-2">
              {items.map((it) => (
                <li key={it.id} className="px-3 py-2 bg-gray-800 rounded text-gray-100">{it.category} — {it.factor_value}</li>
              ))}
            </ul>
          )}
        </div>
      </main>
    </div>
  );
}
