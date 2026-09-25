import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import PageBreadcrumb from '../../../shared/components/common/PageBreadCrumb';
import PageMeta from '../../../shared/components/common/PageMeta';
import Button from '../../../shared/components/ui/button/Button';
import UserStatusBadge from '../components/UserStatusBadge';
import UserRoleBadge from '../components/UserRoleBadge';
import { userApi } from '../services/userApi';
import type { UserItem } from '../types';

export default function UserDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [user, setUser] = useState<UserItem | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [copiedField, setCopiedField] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;
    const fetchEmployee = async () => {
      if (!id) return;
      setIsLoading(true);
      setError(null);
      try {
        const response = await userApi.getById(id);
        if (isMounted) {
          setUser(response.data);
        }
      } catch (err: any) {
        if (isMounted) {
          const msg =
            err.response?.data?.message ||
            err.response?.data?.detail ||
            err.message ||
            'Failed to load employee details';
          setError(msg);
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    fetchEmployee();
    return () => {
      isMounted = false;
    };
  }, [id]);

  const handleCopy = (text: string, field: string) => {
    navigator.clipboard.writeText(text);
    setCopiedField(field);
    setTimeout(() => setCopiedField(null), 2000);
  };

  const getInitials = (name?: string) => {
    if (!name) return 'U';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
    }
    return (name[0] || 'U').toUpperCase();
  };

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return '—';
    try {
      return new Date(dateStr).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      });
    } catch {
      return dateStr;
    }
  };

  const formatDateTime = (dateStr?: string) => {
    if (!dateStr) return '—';
    try {
      return new Date(dateStr).toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return dateStr;
    }
  };

  if (isLoading) {
    return (
      <div className="flex h-[calc(100vh-220px)] items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 border-4 border-brand-500 border-t-transparent rounded-full animate-spin" />
          <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Loading employee details...</p>
        </div>
      </div>
    );
  }

  if (error || !user) {
    return (
      <div className="flex h-[calc(100vh-220px)] flex-col items-center justify-center gap-4 text-center px-4">
        <div className="flex items-center justify-center w-16 h-16 rounded-2xl bg-red-50 dark:bg-red-950/40 text-red-500">
          <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <h3 className="text-xl font-bold text-gray-900 dark:text-white">Employee Record Not Found</h3>
        <p className="text-sm text-gray-500 dark:text-gray-400 max-w-md">{error || 'The employee record you are looking for does not exist or may have been deleted.'}</p>
        <Link
          to="/users"
          className="mt-2 inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-brand-500 text-white font-medium text-sm hover:bg-brand-600 transition-colors shadow-sm"
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Back to Staff Directory
        </Link>
      </div>
    );
  }

  return (
    <>
      <PageMeta
        title={`${user.fullName} | Employee Details | Materia ERP`}
        description={`View detailed profile and employment record for ${user.fullName}`}
      />
      <PageBreadcrumb
        pageTitle={user.fullName}
        parentName="Staff & Users"
        parentUrl="/users"
      />

      <div className="space-y-6 mt-4">
        {/* Hero Profile Card */}
        <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03] shadow-xs">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
            {/* Avatar & Identifiers */}
            <div className="flex items-center gap-5">
              <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-brand-500 to-brand-700 text-white font-bold flex items-center justify-center text-2xl shadow-md shrink-0 ring-4 ring-brand-50 dark:ring-brand-950/40">
                {getInitials(user.fullName)}
              </div>
              <div className="space-y-1.5">
                <div className="flex flex-wrap items-center gap-2.5">
                  <h2 className="text-2xl font-bold text-gray-900 dark:text-white tracking-tight">
                    {user.fullName}
                  </h2>
                  <span className="inline-flex items-center px-2.5 py-0.5 rounded-lg text-xs font-mono font-bold bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-200 border border-gray-200 dark:border-gray-700">
                    {user.code}
                  </span>
                </div>
                <div className="flex flex-wrap items-center gap-2">
                  <UserStatusBadge status={user.status} />
                  <UserRoleBadge role={user.role} hasAccount={!!user.userId} />
                </div>
              </div>
            </div>

            {/* Actions */}
            <div className="flex items-center gap-3">
              <Button
                variant="outline"
                size="sm"
                onClick={() => navigate('/users')}
                className="flex items-center gap-2"
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                </svg>
                Staff Directory
              </Button>
              <Button
                size="sm"
                onClick={() => navigate(`/users/edit/${user.id}`)}
                className="flex items-center gap-2"
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                </svg>
                Edit Employee
              </Button>
            </div>
          </div>
        </div>

        {/* Details Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {/* Card 1: Personal & Contact Information */}
          <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03] space-y-4">
            <div className="flex items-center gap-2 border-b border-gray-100 pb-3 dark:border-gray-800">
              <span className="flex items-center justify-center w-7 h-7 rounded-lg bg-brand-50 text-brand-600 dark:bg-brand-500/15">
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </span>
              <h3 className="font-semibold text-gray-800 dark:text-white text-base">
                Personal & Contact
              </h3>
            </div>

            <div className="space-y-3.5 text-sm">
              <div>
                <span className="text-xs font-medium text-gray-400 dark:text-gray-500 uppercase tracking-wider block mb-0.5">
                  First Name
                </span>
                <span className="font-medium text-gray-800 dark:text-gray-200">
                  {user.firstName || '—'}
                </span>
              </div>

              <div>
                <span className="text-xs font-medium text-gray-400 dark:text-gray-500 uppercase tracking-wider block mb-0.5">
                  Last Name
                </span>
                <span className="font-medium text-gray-800 dark:text-gray-200">
                  {user.lastName || '—'}
                </span>
              </div>

              <div>
                <span className="text-xs font-medium text-gray-400 dark:text-gray-500 uppercase tracking-wider block mb-0.5">
                  Business Email
                </span>
                <div className="flex items-center justify-between gap-2">
                  <a
                    href={`mailto:${user.email}`}
                    className="font-medium text-brand-600 hover:text-brand-700 dark:text-brand-400 dark:hover:underline truncate"
                  >
                    {user.email}
                  </a>
                  <button
                    onClick={() => handleCopy(user.email, 'email')}
                    title="Copy Email"
                    className="p-1 rounded text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                  >
                    {copiedField === 'email' ? (
                      <span className="text-xs text-green-500 font-bold">Copied!</span>
                    ) : (
                      <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                      </svg>
                    )}
                  </button>
                </div>
              </div>

              <div>
                <span className="text-xs font-medium text-gray-400 dark:text-gray-500 uppercase tracking-wider block mb-0.5">
                  Phone Number
                </span>
                {user.phone ? (
                  <a
                    href={`tel:${user.phone}`}
                    className="font-medium text-gray-800 dark:text-gray-200 hover:text-brand-600 dark:hover:text-brand-400"
                  >
                    {user.phone}
                  </a>
                ) : (
                  <span className="text-gray-400 italic">Not provided</span>
                )}
              </div>
            </div>
          </div>

          {/* Card 2: Employment Details */}
          <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03] space-y-4">
            <div className="flex items-center gap-2 border-b border-gray-100 pb-3 dark:border-gray-800">
              <span className="flex items-center justify-center w-7 h-7 rounded-lg bg-emerald-50 text-emerald-600 dark:bg-emerald-500/15">
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
              </span>
              <h3 className="font-semibold text-gray-800 dark:text-white text-base">
                Employment Status
              </h3>
            </div>

            <div className="space-y-3.5 text-sm">
              <div>
                <span className="text-xs font-medium text-gray-400 dark:text-gray-500 uppercase tracking-wider block mb-0.5">
                  Employee Code
                </span>
                <span className="font-mono font-bold text-gray-800 dark:text-gray-200">
                  {user.code}
                </span>
              </div>

              <div>
                <span className="text-xs font-medium text-gray-400 dark:text-gray-500 uppercase tracking-wider block mb-0.5">
                  Current Status
                </span>
                <div className="mt-1">
                  <UserStatusBadge status={user.status} />
                </div>
              </div>

              <div>
                <span className="text-xs font-medium text-gray-400 dark:text-gray-500 uppercase tracking-wider block mb-0.5">
                  Hire Date
                </span>
                <span className="font-medium text-gray-800 dark:text-gray-200">
                  {formatDate(user.hireDate)}
                </span>
              </div>

              {user.terminationDate && (
                <div>
                  <span className="text-xs font-medium text-red-500 uppercase tracking-wider block mb-0.5">
                    Termination Date
                  </span>
                  <span className="font-medium text-red-600 dark:text-red-400">
                    {formatDate(user.terminationDate)}
                  </span>
                  {user.terminationReason && (
                    <p className="text-xs text-gray-500 mt-1 italic">
                      Reason: {user.terminationReason}
                    </p>
                  )}
                </div>
              )}
            </div>
          </div>

          {/* Card 3: System Access & Security */}
          <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03] space-y-4">
            <div className="flex items-center gap-2 border-b border-gray-100 pb-3 dark:border-gray-800">
              <span className="flex items-center justify-center w-7 h-7 rounded-lg bg-indigo-50 text-indigo-600 dark:bg-indigo-500/15">
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                </svg>
              </span>
              <h3 className="font-semibold text-gray-800 dark:text-white text-base">
                System Credentials & Access
              </h3>
            </div>

            <div className="space-y-3.5 text-sm">
              <div>
                <span className="text-xs font-medium text-gray-400 dark:text-gray-500 uppercase tracking-wider block mb-0.5">
                  Platform Credentials
                </span>
                {user.userId ? (
                  <span className="inline-flex items-center gap-1.5 text-xs font-semibold text-green-700 bg-green-50 dark:bg-green-950/40 dark:text-green-400 px-2.5 py-1 rounded-md">
                    <span className="w-1.5 h-1.5 rounded-full bg-green-500"></span>
                    Provisioned & Active
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-1.5 text-xs font-medium text-gray-500 bg-gray-100 dark:bg-gray-800 dark:text-gray-400 px-2.5 py-1 rounded-md">
                    <span className="w-1.5 h-1.5 rounded-full bg-gray-400"></span>
                    No Portal Account
                  </span>
                )}
              </div>

              <div>
                <span className="text-xs font-medium text-gray-400 dark:text-gray-500 uppercase tracking-wider block mb-0.5">
                  Assigned ERP Role
                </span>
                <div className="mt-1">
                  <UserRoleBadge role={user.role} hasAccount={!!user.userId} />
                </div>
              </div>

              {user.userId && (
                <div>
                  <span className="text-xs font-medium text-gray-400 dark:text-gray-500 uppercase tracking-wider block mb-0.5">
                    Auth User UUID
                  </span>
                  <div className="flex items-center justify-between gap-2">
                    <span className="font-mono text-xs text-gray-600 dark:text-gray-400 truncate">
                      {user.userId}
                    </span>
                    <button
                      onClick={() => handleCopy(user.userId!, 'userId')}
                      title="Copy User ID"
                      className="p-1 rounded text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                    >
                      {copiedField === 'userId' ? (
                        <span className="text-xs text-green-500 font-bold">Copied!</span>
                      ) : (
                        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                        </svg>
                      )}
                    </button>
                  </div>
                </div>
              )}

              <div>
                <span className="text-xs font-medium text-gray-400 dark:text-gray-500 uppercase tracking-wider block mb-0.5">
                  Record Created
                </span>
                <span className="text-xs text-gray-500 dark:text-gray-400">
                  {formatDateTime(user.createdAt)}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
