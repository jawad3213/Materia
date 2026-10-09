import PageBreadcrumb from '../../../shared/components/common/PageBreadCrumb';
import PageMeta from '../../../shared/components/common/PageMeta';
import { Modal } from '../../../shared/components/ui/modal';
import { useModal } from '../../../shared/hooks/useModal';
import ProfileForm from '../components/ProfileForm';
import ChangePasswordForm from '../components/ChangePasswordForm';
import useProfile from '../hooks/useProfile';
import type { UserProfile } from '../types/profile.types';

const ROLE_LABELS: Record<string, string> = {
  ADMIN: 'Administrator',
  PURCHASER: 'Purchaser',
  RECEIVER: 'Receiver',
};

const ROLE_BADGE: Record<string, string> = {
  ADMIN: 'bg-purple-50 text-purple-700 border-purple-200 dark:bg-purple-950/40 dark:text-purple-300 dark:border-purple-800',
  PURCHASER: 'bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-950/40 dark:text-blue-300 dark:border-blue-800',
  RECEIVER: 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-800',
};

function initials(profile: UserProfile): string {
  const fromNames = `${profile.firstName?.[0] ?? ''}${profile.lastName?.[0] ?? ''}`;
  return (fromNames || profile.email.slice(0, 2)).toUpperCase();
}

function formatDate(value: string | null): string {
  if (!value) return '—';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '—' : date.toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' });
}

function Field({ label, value }: { label: string; value: string | null | undefined }) {
  return (
    <div>
      <p className="mb-2 text-xs leading-normal text-gray-500 dark:text-gray-400">{label}</p>
      <p className="text-sm font-medium text-gray-800 dark:text-white/90">{value || '—'}</p>
    </div>
  );
}

const cardClass = 'p-5 border border-gray-200 rounded-2xl dark:border-gray-800 lg:p-6';

export default function ProfilePage() {
  const { profile, isLoading, error, reload, save } = useProfile();
  const { isOpen, openModal, closeModal } = useModal();

  return (
    <>
      <PageMeta title="Profile | Materia" description="View and edit your Materia account" />
      <PageBreadcrumb pageTitle="Profile" />
      <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] lg:p-6">
        <h3 className="mb-5 text-lg font-semibold text-gray-800 dark:text-white/90 lg:mb-7">My Profile</h3>

        {isLoading && <p className="text-sm text-gray-500 dark:text-gray-400">Loading your profile…</p>}

        {!isLoading && error && (
          <div className="flex flex-col items-start gap-3 rounded-lg border border-error-200 bg-error-50 px-4 py-3 text-sm text-error-700 dark:border-error-800 dark:bg-error-500/10 dark:text-error-400">
            {error}
            <button onClick={reload} className="font-medium underline">Try again</button>
          </div>
        )}

        {!isLoading && profile && (
          <div className="space-y-6">
            <div className={cardClass}>
              <div className="flex flex-col items-center gap-6 xl:flex-row">
                <div
                  className={`flex h-20 w-20 shrink-0 items-center justify-center rounded-full border text-2xl font-bold ${ROLE_BADGE[profile.roleCode] ?? ROLE_BADGE.RECEIVER}`}
                >
                  {initials(profile)}
                </div>
                <div className="text-center xl:text-left">
                  <h4 className="mb-2 text-lg font-semibold text-gray-800 dark:text-white/90">
                    {profile.fullName || profile.email}
                  </h4>
                  <div className="flex flex-col items-center gap-2 xl:flex-row xl:gap-3">
                    <span className={`rounded border px-1.5 py-0.5 text-[10px] font-bold uppercase tracking-wider ${ROLE_BADGE[profile.roleCode] ?? ''}`}>
                      {ROLE_LABELS[profile.roleCode] ?? profile.roleCode}
                    </span>
                    {profile.department && (
                      <>
                        <div className="hidden h-3.5 w-px bg-gray-300 dark:bg-gray-700 xl:block" />
                        <p className="text-sm text-gray-500 dark:text-gray-400">{profile.department}</p>
                      </>
                    )}
                    <div className="hidden h-3.5 w-px bg-gray-300 dark:bg-gray-700 xl:block" />
                    <p className="text-sm text-gray-500 dark:text-gray-400">{profile.email}</p>
                  </div>
                </div>
              </div>
            </div>

            <div className={cardClass}>
              <div className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
                <div className="w-full">
                  <h4 className="text-lg font-semibold text-gray-800 dark:text-white/90 lg:mb-6">Personal Information</h4>
                  <div className="grid grid-cols-1 gap-4 lg:grid-cols-2 lg:gap-7 2xl:gap-x-32">
                    <Field label="First Name" value={profile.firstName} />
                    <Field label="Last Name" value={profile.lastName} />
                    <Field label="Email address" value={profile.email} />
                    <Field label="Phone" value={profile.phone} />
                    <Field label="Role" value={ROLE_LABELS[profile.roleCode] ?? profile.roleCode} />
                    <Field label="Member since" value={formatDate(profile.createdAt)} />
                  </div>
                </div>
                <button
                  onClick={openModal}
                  className="flex w-full items-center justify-center gap-2 rounded-full border border-gray-300 bg-white px-4 py-3 text-sm font-medium text-gray-700 shadow-theme-xs hover:bg-gray-50 hover:text-gray-800 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-400 dark:hover:bg-white/[0.03] dark:hover:text-gray-200 lg:inline-flex lg:w-auto"
                >
                  <svg className="fill-current" width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path
                      fillRule="evenodd"
                      clipRule="evenodd"
                      d="M15.0911 2.78206C14.2125 1.90338 12.7878 1.90338 11.9092 2.78206L4.57524 10.116C4.26682 10.4244 4.0547 10.8158 3.96468 11.2426L3.31231 14.3352C3.25997 14.5833 3.33653 14.841 3.51583 15.0203C3.69512 15.1996 3.95286 15.2761 4.20096 15.2238L7.29355 14.5714C7.72031 14.4814 8.11172 14.2693 8.42013 13.9609L15.7541 6.62695C16.6327 5.74827 16.6327 4.32365 15.7541 3.44497L15.0911 2.78206ZM12.9698 3.84272C13.2627 3.54982 13.7376 3.54982 14.0305 3.84272L14.6934 4.50563C14.9863 4.79852 14.9863 5.2734 14.6934 5.56629L14.044 6.21573L12.3204 4.49215L12.9698 3.84272ZM11.2597 5.55281L5.6359 11.1766C5.53309 11.2794 5.46238 11.4099 5.43238 11.5522L5.01758 13.5185L6.98394 13.1037C7.1262 13.0737 7.25666 13.003 7.35947 12.9002L12.9833 7.27639L11.2597 5.55281Z"
                      fill=""
                    />
                  </svg>
                  Edit
                </button>
              </div>
            </div>

            <div className={cardClass}>
              <h4 className="mb-6 text-lg font-semibold text-gray-800 dark:text-white/90">Change Password</h4>
              <ChangePasswordForm />
            </div>
          </div>
        )}
      </div>

      {profile && (
        <Modal isOpen={isOpen} onClose={closeModal} className="max-w-[700px] m-4">
          <div className="no-scrollbar relative w-full max-w-[700px] overflow-y-auto rounded-3xl bg-white p-4 dark:bg-gray-900 lg:p-11">
            <div className="px-2 pr-14">
              <h4 className="mb-2 text-2xl font-semibold text-gray-800 dark:text-white/90">Edit Personal Information</h4>
              <p className="mb-6 text-sm text-gray-500 dark:text-gray-400 lg:mb-7">
                Update your details to keep your profile up-to-date.
              </p>
            </div>
            {/* Remount on each open so the form starts from the saved values. */}
            {isOpen && <ProfileForm profile={profile} onSave={save} onCancel={closeModal} />}
          </div>
        </Modal>
      )}
    </>
  );
}
