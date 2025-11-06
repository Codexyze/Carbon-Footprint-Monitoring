"use client";
import React, { useEffect, useState } from "react";
import { fetchUsers, createUser } from "@/lib/tenants/api";
import DashboardHeader from '@/components/dashboard/Header';
import type { TenantUser } from "@/types/tenants/tenantstypes";

export default function TenantUsersPage() {
  const [items, setItems] = useState<TenantUser[]>([]);
  const [loading, setLoading] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("admin");
  const [error, setError] = useState<string | null>(null);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const res = await fetchUsers();
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
    try {
      await createUser({ email, password, role });
      // reload list
      await load();
      setEmail("");
      setPassword("");
    } catch (err) {
      setError(String(err));
    }
  }

  useEffect(() => {
    load();
  }, []);

  return (
    <div className="min-h-screen bg-linear-to-br from-gray-900 via-emerald-950 to-gray-900">
      <DashboardHeader />
      <main className="max-w-7xl mx-auto px-6 py-8">
        <h1 className="text-2xl font-semibold text-gray-100 mb-6">Tenant Users</h1>

        <section className="mb-6">
          <div className="max-w-xl bg-gray-800/30 p-6 rounded-md">
            <h2 className="text-lg font-medium text-gray-100 mb-3">Create User</h2>
            <form onSubmit={onCreate} className="space-y-3">
              <input placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} className="w-full px-3 py-2 bg-gray-900 border border-gray-700 rounded text-gray-100 focus:outline-none focus:ring-2 focus:ring-emerald-400" />
              <input placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} type="password" className="w-full px-3 py-2 bg-gray-900 border border-gray-700 rounded text-gray-100 focus:outline-none focus:ring-2 focus:ring-emerald-400" />
              <select value={role} onChange={(e) => setRole(e.target.value)} className="w-full px-3 py-2 bg-gray-900 border border-gray-700 rounded text-gray-100">
                <option value="admin">admin</option>
                <option value="user">user</option>
              </select>
              <div>
                <button type="submit" className="px-4 py-2 bg-emerald-600 rounded text-white">Create User</button>
              </div>
            </form>
            {error && <div className="text-red-400 mt-2">{error}</div>}
          </div>
        </section>

        <section>
          <h2 className="text-lg font-medium text-gray-100 mb-3">Users</h2>
          {loading ? (
            <div className="text-gray-300">Loading...</div>
          ) : (
            <ul className="space-y-2">
              {items.map((it) => (
                <li key={it.id} className="p-3 bg-gray-800/20 border border-gray-700 rounded flex justify-between items-center">
                  <div>
                    <div className="font-medium text-gray-100">{it.email}</div>
                    <div className="text-sm text-gray-400">{it.role} — {it.is_active ? 'active' : 'inactive'}</div>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </section>
      </main>
    </div>
  );
}
