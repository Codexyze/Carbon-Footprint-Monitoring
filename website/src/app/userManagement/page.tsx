// app/user-management/page.tsx
 'use client';

import { useEffect, useState } from 'react';
import UserTable from '@/ui/userManagement/Table';
import SearchBar from '@/ui/userManagement/SearchBar';
import { User } from '@/types/auth/user';
import DashboardHeader from '@/components/dashboard/Header';
import { createUser, fetchUsers } from '@/lib/tenants/api';
import type { TenantUser, CreateUserRequest } from '@/types/tenants/tenantstypes';
import { Logger } from '@/lib/logger';

export default function UserManagementPage() {
  const [searchQuery, setSearchQuery] = useState('');
  const [users, setUsers] = useState<TenantUser[]>([]);
  const [loadingUsers, setLoadingUsers] = useState(false);
  const [inviteOpen, setInviteOpen] = useState(false);
  const [inviteEmail, setInviteEmail] = useState('');
  const [inviteRole, setInviteRole] = useState<'admin' | 'user' | 'viewer'>('user');
  const [invitePassword, setInvitePassword] = useState('');
  const [inviteLoading, setInviteLoading] = useState(false);
  const [inviteError, setInviteError] = useState<string | null>(null);

  // Load users from tenants API if available
  async function loadUsers() {
    setLoadingUsers(true);
    try {
      const list = await fetchUsers();
      setUsers(list);
    } catch (err) {
      Logger.w('USER_MGMT', `Failed to fetch users: ${String(err)} — falling back to sample data`);
      // fallback sample
      setUsers([
        { id: 1, email: 'eleanor@example.com', role: 'admin', is_active: true },
        { id: 2, email: 'marcus@example.com', role: 'user', is_active: true },
      ] as TenantUser[]);
    } finally {
      setLoadingUsers(false);
    }
  }

  useEffect(() => {
    loadUsers();
  }, []);

  const filteredUsers = users.filter(
    (u) => (u.email ?? '').toLowerCase().includes(searchQuery.toLowerCase()) || (u.role ?? '').toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="min-h-screen bg-[#0a1a1a] text-white">
      <DashboardHeader />
      <div className="max-w-7xl mx-auto p-8">
        <div className="flex justify-between items-start mb-8">
          <div>
            <h1 className="text-5xl font-bold mb-2">User Management</h1>
            <p className="text-gray-400">
              Invite, manage roles, and view activity for all users in your organization.
            </p>
          </div>
          <button onClick={() => setInviteOpen(true)} className="bg-[#2dd4bf] hover:bg-[#14b8a6] text-black font-semibold px-6 py-3 rounded-lg flex items-center gap-2 transition-colors">
            <span className="text-xl">+</span>
            Invite User
          </button>
        </div>

        <SearchBar searchQuery={searchQuery} setSearchQuery={setSearchQuery} />
        <UserTable users={filteredUsers as any} totalUsers={filteredUsers.length} />

        {/* Invite modal */}
        {inviteOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60">
            <div className="bg-gray-900 p-6 rounded-lg w-full max-w-md">
              <h2 className="text-xl font-semibold mb-4">Invite User</h2>
              <form
                onSubmit={async (e) => {
                  e.preventDefault();
                  setInviteError(null);
                  setInviteLoading(true);
                  try {
                    const payload: CreateUserRequest = { email: inviteEmail, password: invitePassword, role: inviteRole };
                    const u = await createUser(payload);
                    // Append to list
                    setUsers((prev) => [u, ...prev]);
                    setInviteOpen(false);
                    setInviteEmail('');
                    setInvitePassword('');
                    Logger.i('USER_MGMT', `Invited user ${u.email}`);
                  } catch (err: any) {
                    setInviteError(String(err?.message ?? err ?? 'Failed'));
                    Logger.e('USER_MGMT', String(err));
                  } finally {
                    setInviteLoading(false);
                  }
                }}
                className="space-y-3"
              >
                <div>
                  <label className="block text-sm text-gray-300 mb-1">Email</label>
                  <input required type="email" value={inviteEmail} onChange={(e) => setInviteEmail(e.target.value)} className="w-full p-2 bg-gray-800 border rounded" />
                </div>

                <div>
                  <label className="block text-sm text-gray-300 mb-1">Role</label>
                  <select value={inviteRole} onChange={(e) => setInviteRole(e.target.value as any)} className="w-full p-2 bg-gray-800 border rounded">
                    <option value="admin">Admin</option>
                    <option value="user">User</option>
                    <option value="viewer">Viewer</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm text-gray-300 mb-1">Temporary Password</label>
                  <input required type="password" value={invitePassword} onChange={(e) => setInvitePassword(e.target.value)} className="w-full p-2 bg-gray-800 border rounded" />
                </div>

                {inviteError && <div className="text-sm text-red-400">{inviteError}</div>}

                <div className="flex items-center justify-end gap-2">
                  <button type="button" onClick={() => setInviteOpen(false)} className="px-3 py-2 rounded bg-gray-700">Cancel</button>
                  <button type="submit" disabled={inviteLoading} className="px-4 py-2 bg-emerald-600 rounded">{inviteLoading ? 'Inviting…' : 'Invite'}</button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}