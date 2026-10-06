import { useState } from 'react';
import PageBreadcrumb from '../../../shared/components/common/PageBreadCrumb';
import PageMeta from '../../../shared/components/common/PageMeta';
import Badge from '../../../shared/components/ui/badge/Badge';
import Button from '../../../shared/components/ui/button/Button';
import { Modal } from '../../../shared/components/ui/modal';
import { DetailCard, DetailField, DetailGrid, FloatingToast, PageLoader } from '../../../shared/components/page/DetailParts';
import { InitialsAvatar } from '../../../shared/components/page/ListParts';
import { SectionIcons } from '../../../shared/components/page/pageIcons';
import ChangePasswordForm from '../components/ChangePasswordForm';
import ProfileForm from '../components/ProfileForm';
import { useProfile } from '../hooks/useProfile';
import type { Profile } from '../types/profile.types';

type Feedback = { type: 'success' | 'error'; text: string } | null;

const EDIT_ICON = (
  <svg className="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2} aria-hidden="true">
    <path strokeLinecap="round" strokeLinejoin="round" d="M16.862 4.487a2.1 2.1 0 1 1 2.97 2.97L8.5 18.79l-4 1 1-4 11.362-11.303Z" />
  </svg>
);

function formatDate(value: string | null) {
  if (!value) return '—';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '—' : date.toLocaleDateString('en-GB', { day: 'numeric', month: 'long', year: 'numeric' });
}

function displayName(profile: Profile) {
  return profile.fullName || [profile.firstName, profile.lastName].filter(Boolean).join(' ') || profile.email;
}

/** Permissions grouped by area: "order:read", "order:write" -> order: read, write. */
function groupPermissions(permissions: string[]) {
  const groups = new Map<string, string[]>();
  permissions.forEach((permission) => {
    const [area, ...rest] = permission.split(':');
    const action = rest.join(' ') || 'access';
    groups.set(area, [...(groups.get(area) ?? []), action]);
  });
  return [...groups.entries()].sort(([a], [b]) => a.localeCompare(b));
}

const value = (text: string | null | undefined) => (text && text.trim() ? text : <span className="text-gray-400">Not set</span>);

export default function ProfilePage() {
  const { profile, error, loading, reload, save } = useProfile();
  const [editing, setEditing] = useState(false);
  const [feedback, setFeedback] = useState<Feedback>(null);

  const handleSave = async (payload: Parameters<typeof save>[0]) => {
    await save(payload);
    setEditing(false);
    setFeedback({ type: 'success', text: 'Your profile has been updated' });
  };

  return (
    <>
      <PageMeta title="My profile | Materia" description="Your Materia account: personal information, role and password." />
      <PageBreadcrumb pageTitle="My profile" />

      {loading && !profile ? (
        <PageLoader message="Loading your profile…" />
      ) : error || !profile ? (
        <div className="rounded-2xl border border-gray-200 bg-white p-10 text-center dark:border-gray-800 dark:bg-white/[0.03]">
          <p className="text-base font-semibold text-gray-800 dark:text-white/90">Your profile could not be loaded</p>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">{error}</p>
          <div className="mt-5 flex justify-center">
            <Button size="sm" onClick={reload}>
              Try again
            </Button>
          </div>
        </div>
      ) : (
        <div className="space-y-6">
          {/* Identity */}
          <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] lg:p-6">
            <div className="flex flex-col items-center gap-5 text-center sm:flex-row sm:text-left">
              <InitialsAvatar name={displayName(profile)} size="lg" />
              <div className="min-w-0 flex-1">
                <h3 className="truncate text-xl font-semibold text-gray-800 dark:text-white/90">{displayName(profile)}</h3>
                <div className="mt-1.5 flex flex-wrap items-center justify-center gap-x-3 gap-y-1.5 text-sm text-gray-500 sm:justify-start dark:text-gray-400">
                  {profile.roleLabel && (
                    <Badge size="sm" color="primary">
                      {profile.roleLabel}
                    </Badge>
                  )}
                  {profile.department && <span>{profile.department}</span>}
                  <span className="truncate">{profile.email}</span>
                </div>
              </div>
              <Button size="sm" variant="outline" startIcon={EDIT_ICON} onClick={() => setEditing(true)}>
                Edit profile
              </Button>
            </div>
          </div>

          <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
            <DetailCard
              title="Personal information"
              icon={SectionIcons.info}
              tone="brand"
              aside={
                <button type="button" onClick={() => setEditing(true)} className="text-sm font-medium text-brand-500 hover:text-brand-600">
                  Edit
                </button>
              }
            >
              <DetailGrid>
                <DetailField label="First name">{value(profile.firstName)}</DetailField>
                <DetailField label="Last name">{value(profile.lastName)}</DetailField>
                <DetailField label="Phone">{value(profile.phone)}</DetailField>
                <DetailField label="E-mail">{profile.email}</DetailField>
              </DetailGrid>
            </DetailCard>

            <DetailCard title="Account" icon={SectionIcons.check} tone="gray" aside={<span className="text-xs text-gray-400">Managed by an administrator</span>}>
              <DetailGrid>
                <DetailField label="Role">{value(profile.roleLabel ?? profile.role)}</DetailField>
                <DetailField label="Department">{value(profile.department)}</DetailField>
                <DetailField label="Status">
                  <Badge size="sm" color={profile.status === 'ACTIVE' ? 'success' : 'warning'}>
                    {profile.status ? profile.status.charAt(0) + profile.status.slice(1).toLowerCase() : 'Unknown'}
                  </Badge>
                </DetailField>
                <DetailField label="Member since">{formatDate(profile.createdAt)}</DetailField>
              </DetailGrid>
            </DetailCard>
          </div>

          <DetailCard title="Password" icon={SectionIcons.note} tone="warning">
            <ChangePasswordForm onChanged={(text) => setFeedback({ type: 'success', text })} />
          </DetailCard>

          {profile.permissions.length > 0 && (
            <DetailCard title="What your role allows" icon={SectionIcons.list} tone="success">
              <ul className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-3">
                {groupPermissions(profile.permissions).map(([area, actions]) => (
                  <li key={area} className="rounded-xl border border-gray-100 p-3 dark:border-white/5">
                    <p className="text-sm font-semibold capitalize text-gray-800 dark:text-white/90">{area}</p>
                    <p className="mt-1 flex flex-wrap gap-1.5">
                      {actions.map((action) => (
                        <span key={action} className="rounded-md bg-gray-100 px-2 py-0.5 text-xs text-gray-600 dark:bg-white/5 dark:text-gray-300">
                          {action}
                        </span>
                      ))}
                    </p>
                  </li>
                ))}
              </ul>
            </DetailCard>
          )}
        </div>
      )}

      {profile && (
        <Modal isOpen={editing} onClose={() => setEditing(false)} className="m-4 max-w-[700px]">
          <div className="no-scrollbar relative w-full overflow-y-auto rounded-3xl bg-white p-4 dark:bg-gray-900 lg:p-11">
            {editing && <ProfileForm profile={profile} onSave={handleSave} onCancel={() => setEditing(false)} />}
          </div>
        </Modal>
      )}

      <FloatingToast feedback={feedback} onClose={() => setFeedback(null)} />
    </>
  );
}
