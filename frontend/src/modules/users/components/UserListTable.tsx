import React, { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Table,
  TableHeader,
  TableBody,
  TableRow,
  TableCell,
} from '../../../shared/components/ui/table';
import Button from '../../../shared/components/ui/button/Button';
import InputField from '../../../shared/components/form/input/InputField';
import Select from '../../../shared/components/form/Select';
import DeleteConfirmModal from '../../../shared/components/ui/modal/DeleteConfirmModal';
import UserStatusBadge from './UserStatusBadge';
import UserRoleBadge from './UserRoleBadge';
import OffboardUserModal from './OffboardUserModal';
import { useUsers } from '../hooks/useUsers';
import { EmploymentStatus } from '../enums/EmploymentStatus';
import type { UserListItem, UpdateUserRequest, OffboardUserRequest } from '../types';

export const UserListTable: React.FC = () => {
  const navigate = useNavigate();
  const {
    users,
    isLoading,
    error,
    fetchUsers,
    onboard,
    update,
    offboard,
    remove,
  } = useUsers();

  // Search & Filter state
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');

  // Modal states
  const [userToOffboard, setUserToOffboard] = useState<UserItem | null>(null);
  const [userToDelete, setUserToDelete] = useState<UserItem | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  // Statistics
  const stats = useMemo(() => {
    return {
      total: users.length,
      active: users.filter((u) => u.status === EmploymentStatus.ACTIVE).length,
      probation: users.filter((u) => u.status === EmploymentStatus.PROBATION).length,
      terminated: users.filter((u) => u.status === EmploymentStatus.TERMINATED).length,
    };
  }, [users]);

  // Filtered users
  const filteredUsers = useMemo(() => {
    return users.filter((u) => {
      const q = searchQuery.toLowerCase().trim();
      const matchesSearch =
        !q ||
        u.fullName.toLowerCase().includes(q) ||
        u.email.toLowerCase().includes(q) ||
        u.code.toLowerCase().includes(q);

      const matchesStatus = !selectedStatus || u.status === selectedStatus;

      return matchesSearch && matchesStatus;
    });
  }, [users, searchQuery, selectedStatus]);

  // Handlers
  const handleOffboardSubmit = async (data: OffboardUserRequest) => {
    if (userToOffboard) {
      await offboard(data);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!userToDelete) return;
    setIsDeleting(true);
    try {
      await remove(userToDelete.id);
      setUserToDelete(null);
    } finally {
      setIsDeleting(false);
    }
  };

  // Helper: Initials for avatar
  const getInitials = (name: string) => {
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
    }
    return (name[0] || 'U').toUpperCase();
  };

  const formatCreatedDate = (dateStr?: string) => {
    if (!dateStr) return '—';
    try {
      return new Date(dateStr).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
      });
    } catch {
      return dateStr;
    }
  };

  return (
    <div className="space-y-6">
      {/* Stat Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="p-4 rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-white/[0.03]">
          <span className="text-xs text-gray-500 dark:text-gray-400 font-medium">Total Staff</span>
          <div className="text-2xl font-bold text-gray-800 dark:text-white mt-1">{stats.total}</div>
        </div>
        <div className="p-4 rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-white/[0.03]">
          <span className="text-xs text-success-600 dark:text-success-400 font-medium">Active</span>
          <div className="text-2xl font-bold text-success-600 dark:text-success-400 mt-1">{stats.active}</div>
        </div>
        <div className="p-4 rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-white/[0.03]">
          <span className="text-xs text-warning-600 dark:text-warning-400 font-medium">Probation</span>
          <div className="text-2xl font-bold text-warning-600 dark:text-warning-400 mt-1">{stats.probation}</div>
        </div>
        <div className="p-4 rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-white/[0.03]">
          <span className="text-xs text-gray-400 font-medium">Offboarded</span>
          <div className="text-2xl font-bold text-gray-500 dark:text-gray-400 mt-1">{stats.terminated}</div>
        </div>
      </div>

      {/* Action Bar */}
      <div className="p-5 rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-white/[0.03] space-y-4">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="w-full sm:w-80">
            <InputField
              id="userSearch"
              placeholder="Search by name, email, code..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>

          <div className="flex items-center gap-3 w-full sm:w-auto justify-end">
            <Button
              type="button"
              variant="outline"
              onClick={() => fetchUsers()}
              disabled={isLoading}
            >
              🔄 Refresh
            </Button>
            <Button
              type="button"
              onClick={() => navigate('/users/create')}
            >
              + Onboard Employee
            </Button>
          </div>
        </div>

        {/* Filters */}
        <div className="flex flex-wrap items-center gap-4 pt-3 border-t border-gray-100 dark:border-gray-800">
          <div className="w-44">
            <Select
              id="filterStatus"
              options={[
                { value: '', label: 'All Statuses' },
                { value: EmploymentStatus.ACTIVE, label: 'Active' },
                { value: EmploymentStatus.PROBATION, label: 'Probation' },
                { value: EmploymentStatus.ON_LEAVE, label: 'On Leave' },
                { value: EmploymentStatus.SUSPENDED, label: 'Suspended' },
                { value: EmploymentStatus.TERMINATED, label: 'Terminated' },
              ]}
              value={selectedStatus}
              onChange={(val) => setSelectedStatus(val)}
            />
          </div>

          {(searchQuery || selectedStatus) && (
            <button
              onClick={() => {
                setSearchQuery('');
                setSelectedStatus('');
              }}
              className="text-xs text-brand-600 hover:text-brand-700 dark:text-brand-400 underline cursor-pointer"
            >
              Clear filters
            </button>
          )}

          <div className="ml-auto text-xs text-gray-500">
            Showing <strong>{filteredUsers.length}</strong> of <strong>{users.length}</strong> records
          </div>
        </div>
      </div>

      {/* Error state */}
      {error && (
        <div className="p-4 rounded-xl bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 text-sm">
          {error}
        </div>
      )}

      {/* Users Table */}
      <div className="overflow-hidden rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-white/[0.03]">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader className="bg-gray-50/75 dark:bg-gray-800/40 text-left border-b border-gray-100 dark:border-gray-800">
              <TableRow>
                <TableCell isHeader className="py-3.5 px-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  Full Name
                </TableCell>
                <TableCell isHeader className="py-3.5 px-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  Email
                </TableCell>
                <TableCell isHeader className="py-3.5 px-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  Code
                </TableCell>
                <TableCell isHeader className="py-3.5 px-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  Status
                </TableCell>
                <TableCell isHeader className="py-3.5 px-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  Role
                </TableCell>
                <TableCell isHeader className="py-3.5 px-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  Day of Creation
                </TableCell>
                <TableCell isHeader className="py-3.5 px-4 text-xs font-semibold text-gray-500 uppercase tracking-wider text-right">
                  Actions
                </TableCell>
              </TableRow>
            </TableHeader>

            <TableBody>
              {isLoading ? (
                <TableRow>
                  <TableCell colSpan={7} className="py-12 text-center text-gray-400">
                    <div className="flex flex-col items-center justify-center gap-2">
                      <div className="w-8 h-8 border-2 border-brand-500 border-t-transparent rounded-full animate-spin" />
                      <span>Loading employee records...</span>
                    </div>
                  </TableCell>
                </TableRow>
              ) : filteredUsers.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} className="py-12 text-center text-gray-400">
                    <div className="flex flex-col items-center justify-center gap-2">
                      <span className="text-3xl">👥</span>
                      <p className="text-gray-500 dark:text-gray-400 font-medium">No employees found</p>
                      <p className="text-xs text-gray-400">Try adjusting your search criteria or onboard a new employee.</p>
                    </div>
                  </TableCell>
                </TableRow>
              ) : (
                filteredUsers.map((item) => (
                  <TableRow
                    key={item.id}
                    className="border-b border-gray-100 dark:border-gray-800/60 hover:bg-gray-50/50 dark:hover:bg-white/[0.02] transition-colors"
                  >
                    {/* Full Name */}
                    <TableCell className="py-3.5 px-4">
                      <div className="flex items-center gap-3">
                        <button
                          type="button"
                          onClick={() => navigate(`/users/${item.id}`)}
                          title={`View ${item.fullName}'s profile`}
                          className="w-9 h-9 rounded-full bg-brand-50 dark:bg-brand-500/15 text-brand-600 dark:text-brand-400 font-bold flex items-center justify-center text-xs shrink-0 cursor-pointer hover:ring-2 hover:ring-brand-500 transition-all focus:outline-none"
                        >
                          {getInitials(item.fullName)}
                        </button>
                        <button
                          type="button"
                          onClick={() => navigate(`/users/${item.id}`)}
                          title={`View ${item.fullName}'s profile`}
                          className="font-semibold text-gray-800 dark:text-white text-sm hover:text-brand-600 dark:hover:text-brand-400 transition-colors text-left flex items-center gap-1.5 group cursor-pointer focus:outline-none"
                        >
                          <span>{item.fullName}</span>
                          <svg className="w-3.5 h-3.5 opacity-0 -translate-x-1 group-hover:opacity-100 group-hover:translate-x-0 transition-all text-brand-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                          </svg>
                        </button>
                      </div>
                    </TableCell>

                    {/* Email */}
                    <TableCell className="py-3.5 px-4 text-sm text-gray-600 dark:text-gray-300">
                      <a
                        href={`mailto:${item.email}`}
                        className="hover:text-brand-600 dark:hover:text-brand-400 transition-colors"
                      >
                        {item.email}
                      </a>
                    </TableCell>

                    {/* Code */}
                    <TableCell className="py-3.5 px-4 font-mono text-xs">
                      <button
                        type="button"
                        onClick={() => navigate(`/users/${item.id}`)}
                        className="font-medium text-gray-700 dark:text-gray-300 hover:text-brand-600 dark:hover:text-brand-400 hover:underline cursor-pointer focus:outline-none px-2 py-0.5 rounded bg-gray-100 dark:bg-gray-800/80 border border-gray-200 dark:border-gray-700"
                        title="View employee"
                      >
                        {item.code}
                      </button>
                    </TableCell>

                    {/* Status */}
                    <TableCell className="py-3.5 px-4">
                      <UserStatusBadge status={item.status} />
                    </TableCell>

                    {/* Role */}
                    <TableCell className="py-3.5 px-4">
                      <UserRoleBadge role={item.role} hasAccount={!!item.userId} />
                    </TableCell>

                    {/* Day of Creation */}
                    <TableCell className="py-3.5 px-4 text-xs text-gray-500 dark:text-gray-400 whitespace-nowrap">
                      {formatCreatedDate(item.createdAt)}
                    </TableCell>

                    {/* Actions */}
                    <TableCell className="py-3.5 px-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        {/* View */}
                        <button
                          type="button"
                          onClick={() => navigate(`/users/${item.id}`)}
                          title="View Employee Details"
                          className="p-1.5 rounded-lg text-gray-500 hover:text-brand-600 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
                        >
                          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                          </svg>
                        </button>

                        {/* Edit */}
                        <button
                          type="button"
                          onClick={() => navigate(`/users/edit/${item.id}`)}
                          title="Edit Employee"
                          className="p-1.5 rounded-lg text-gray-500 hover:text-brand-600 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
                        >
                          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                          </svg>
                        </button>

                        {/* Offboard (Only if not already terminated) */}
                        {item.status !== EmploymentStatus.TERMINATED && (
                          <button
                            onClick={() => setUserToOffboard(item)}
                            title="Offboard / Terminate"
                            className="p-1.5 rounded-lg text-amber-500 hover:text-amber-600 hover:bg-amber-50 dark:hover:bg-amber-900/20 transition-colors"
                          >
                            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                            </svg>
                          </button>
                        )}

                        {/* Delete */}
                        <button
                          onClick={() => setUserToDelete(item)}
                          title="Delete Employee"
                          className="p-1.5 rounded-lg text-gray-400 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors"
                        >
                          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                          </svg>
                        </button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      </div>


      <OffboardUserModal
        isOpen={!!userToOffboard}
        user={userToOffboard}
        onClose={() => setUserToOffboard(null)}
        onSuccess={handleOffboardSubmit}
      />

      <DeleteConfirmModal
        isOpen={!!userToDelete}
        onClose={() => setUserToDelete(null)}
        onConfirm={handleDeleteConfirm}
        isDeleting={isDeleting}
        title="Delete Employee Record"
        message={`Are you sure you want to permanently delete ${userToDelete?.fullName} (${userToDelete?.code})? This action cannot be undone.`}
      />
    </div>
  );
};

export default UserListTable;
