import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import PageMeta from '../../../shared/components/common/PageMeta';
import Button from '../../../shared/components/ui/button/Button';
import UserStatusBadge from '../components/UserStatusBadge';
import UserRoleBadge from '../components/UserRoleBadge';
import { userApi } from '../services/userApi';
import type { UserItem } from '../types';
import {
  BackToListButton,
  DetailCard,
  DetailField,
  DetailGrid,
  DetailHeader,
  PageLoader,
  PageNotFound,
} from '../../../shared/components/page/DetailParts';
import { InitialsAvatar } from '../../../shared/components/page/ListParts';
import { SectionIcons } from '../../../shared/components/page/pageIcons';
import { getApiErrorMessage } from '../../../shared/utils/apiError';

const formatDate = (value?: string) => (value ? new Date(value).toLocaleDateString() : '—');
const formatDateTime = (value?: string) => (value ? new Date(value).toLocaleString() : '—');

/** Copies a value and confirms it for two seconds. */
function CopyButton({ value, label }: { value: string; label: string }) {
  const [copied, setCopied] = useState(false);
  return (
    <button
      type="button"
      title={`Copy ${label}`}
      onClick={() => {
        navigator.clipboard.writeText(value);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
      }}
      className="ml-2 text-theme-xs font-medium text-brand-500 hover:underline"
    >
      {copied ? 'Copied' : 'Copy'}
    </button>
  );
}

export default function UserDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [user, setUser] = useState<UserItem | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    userApi
      .getById(id)
      .then((res) => {
        if (!cancelled) setUser(res.data);
      })
      .catch((err) => {
        if (!cancelled) setError(getApiErrorMessage(err, 'The employee record you requested does not exist.'));
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  if (isLoading) return <PageLoader message="Loading employee..." />;
  if (!user) {
    return (
      <PageNotFound
        title="Employee Not Found"
        message={error || 'The employee record you requested does not exist.'}
        backTo="/users"
        backLabel="Back to Staff Directory"
      />
    );
  }

  return (
    <>
      <PageMeta title={`${user.fullName} | Materia Dashboard`} description={`Employment record and system access of ${user.fullName}`} />

      <DetailHeader title={user.fullName} parentName="Staff Directory" parentUrl="/users">
        <BackToListButton to="/users" />
        <Link to={`/users/edit/${user.id}`}>
          <Button size="sm">Edit Employee</Button>
        </Link>
      </DetailHeader>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="flex flex-col gap-6 lg:col-span-1">
          <DetailCard>
            <div className="mb-6 flex items-center justify-between">
              <InitialsAvatar name={user.fullName} size="lg" />
              <div className="flex flex-col items-end gap-2">
                <UserStatusBadge status={user.status} />
                <UserRoleBadge role={user.role} hasAccount={!!user.userId} />
              </div>
            </div>
            <h3 className="mb-1 text-xl font-bold text-gray-900 dark:text-white">{user.fullName}</h3>
            <p className="mb-6 font-mono text-sm font-medium text-gray-500 dark:text-gray-400">{user.code}</p>
            <div className="space-y-4">
              <DetailField label="Email">
                <a href={`mailto:${user.email}`} className="text-brand-500 hover:underline">
                  {user.email}
                </a>
                <CopyButton value={user.email} label="email" />
              </DetailField>
              <DetailField label="Phone">
                {user.phone ? (
                  <a href={`tel:${user.phone}`} className="text-brand-500 hover:underline">
                    {user.phone}
                  </a>
                ) : (
                  '—'
                )}
              </DetailField>
            </div>
          </DetailCard>
        </div>

        <div className="flex flex-col gap-6 lg:col-span-2">
          <DetailCard title="Personal Information" icon={SectionIcons.info}>
            <DetailGrid>
              <DetailField label="First Name">{user.firstName || '—'}</DetailField>
              <DetailField label="Last Name">{user.lastName || '—'}</DetailField>
            </DetailGrid>
          </DetailCard>

          <DetailCard title="Employment" icon={SectionIcons.calendar} tone={user.terminationDate ? 'error' : 'success'}>
            <DetailGrid>
              <DetailField label="Employee Code">
                <span className="font-mono">{user.code}</span>
              </DetailField>
              <DetailField label="Status">
                <UserStatusBadge status={user.status} />
              </DetailField>
              <DetailField label="Hire Date">{formatDate(user.hireDate)}</DetailField>
              <DetailField label="Termination Date">{formatDate(user.terminationDate)}</DetailField>
              {user.terminationReason && (
                <DetailField label="Termination Reason" wide>
                  {user.terminationReason}
                </DetailField>
              )}
            </DetailGrid>
          </DetailCard>

          <DetailCard title="System Access" icon={SectionIcons.check} tone={user.userId ? 'brand' : 'gray'}>
            <DetailGrid>
              <DetailField label="Login">{user.userId ? 'Account active' : 'No system login'}</DetailField>
              <DetailField label="Role">
                <UserRoleBadge role={user.role} hasAccount={!!user.userId} />
              </DetailField>
              {user.userId && (
                <DetailField label="Account ID" wide>
                  <span className="font-mono text-theme-xs">{user.userId}</span>
                  <CopyButton value={user.userId} label="account ID" />
                </DetailField>
              )}
            </DetailGrid>
          </DetailCard>

          <DetailCard title="System Information" icon={SectionIcons.info}>
            <DetailGrid>
              <DetailField label="Created">{formatDateTime(user.createdAt)}</DetailField>
              <DetailField label="Last Updated">{formatDateTime(user.updatedAt)}</DetailField>
            </DetailGrid>
          </DetailCard>
        </div>
      </div>
    </>
  );
}
