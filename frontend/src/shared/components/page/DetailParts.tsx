import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import PageBreadcrumb from "../common/PageBreadCrumb";
import Toast from "../ui/notifications/Toast";

/** Building blocks of the supplier detail page, shared by the procurement detail pages. */

/** Breadcrumb on the left, actions on the right. */
export function DetailHeader({
  title,
  parentName,
  parentUrl,
  children,
}: {
  title: string;
  parentName: string;
  parentUrl: string;
  children?: ReactNode;
}) {
  return (
    <div className="mb-6 flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
      <PageBreadcrumb pageTitle={title} parentName={parentName} parentUrl={parentUrl} />
      {children && <div className="flex flex-wrap items-center gap-3">{children}</div>}
    </div>
  );
}

/** The outline "Back to List" button of the supplier detail page. */
export function BackToListButton({ to }: { to: string }) {
  return (
    <Link
      to={to}
      className="rounded-lg border border-gray-200 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-white/[0.05] dark:bg-white/[0.03] dark:text-gray-300 dark:hover:bg-white/[0.05]"
    >
      Back to List
    </Link>
  );
}

const SECTION_TONES = {
  brand: "bg-brand-50 text-brand-600 dark:bg-brand-500/10 dark:text-brand-400",
  success: "bg-success-50 text-success-600 dark:bg-success-500/10",
  warning: "bg-warning-50 text-warning-600 dark:bg-warning-500/10 dark:text-orange-400",
  error: "bg-error-50 text-error-600 dark:bg-error-500/10",
  gray: "bg-gray-50 text-gray-600 dark:bg-white/[0.05] dark:text-gray-300",
};

/** A white card; with a title it gets the icon header of the supplier detail sections. */
export function DetailCard({
  title,
  icon,
  tone = "gray",
  aside,
  padded = true,
  children,
}: {
  title?: string;
  icon?: ReactNode;
  tone?: keyof typeof SECTION_TONES;
  aside?: ReactNode;
  padded?: boolean;
  children: ReactNode;
}) {
  return (
    <div className="rounded-2xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
      {title && (
        <div className={`flex items-center justify-between gap-3 border-b border-gray-100 dark:border-gray-800 ${padded ? "mx-6 pb-4 pt-6" : "px-6 py-4"}`}>
          <div className="flex items-center gap-2">
            {icon && <div className={`rounded-lg p-2 ${SECTION_TONES[tone]}`}>{icon}</div>}
            <h4 className="text-base font-semibold text-gray-900 dark:text-white">{title}</h4>
          </div>
          {aside}
        </div>
      )}
      <div className={padded ? "p-6" : ""}>{children}</div>
    </div>
  );
}

/** Label above value, as in the supplier detail sections. */
export function DetailField({ label, children, wide = false }: { label: string; children: ReactNode; wide?: boolean }) {
  return (
    <div className={`flex flex-col ${wide ? "md:col-span-2" : ""}`}>
      <span className="mb-1 text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-400">{label}</span>
      <span className="text-sm font-medium text-gray-900 dark:text-white">{children ?? "—"}</span>
    </div>
  );
}

export function DetailGrid({ children }: { children: ReactNode }) {
  return <div className="grid grid-cols-1 gap-x-8 gap-y-6 md:grid-cols-2">{children}</div>;
}

/** Full-height loading state of the detail pages. */
export function PageLoader({ message }: { message: string }) {
  return (
    <div className="flex h-[calc(100vh-200px)] items-center justify-center">
      <div className="flex flex-col items-center gap-3">
        <svg className="h-8 w-8 animate-spin text-brand-500" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
        </svg>
        <p className="text-sm text-gray-500">{message}</p>
      </div>
    </div>
  );
}

/** Full-height "not found" state of the detail pages. */
export function PageNotFound({ title, message, backTo, backLabel }: { title: string; message: string; backTo: string; backLabel: string }) {
  return (
    <div className="flex h-[calc(100vh-200px)] flex-col items-center justify-center gap-4">
      <div className="flex h-16 w-16 items-center justify-center rounded-full bg-error-50 text-error-500 dark:bg-error-500/10">
        <svg className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
      </div>
      <h3 className="text-xl font-medium text-gray-900 dark:text-white">{title}</h3>
      <p className="text-gray-500 dark:text-gray-400">{message}</p>
      <Link to={backTo} className="mt-4 text-brand-500 hover:underline">
        &larr; {backLabel}
      </Link>
    </div>
  );
}

/** The floating toast used by the supplier forms for success and error messages. */
export function FloatingToast({
  feedback,
  onClose,
}: {
  feedback: { type: "success" | "error"; text: string } | null;
  onClose: () => void;
}) {
  if (!feedback) return null;
  return (
    <div className="fixed right-6 top-20 z-[999999]">
      <Toast variant={feedback.type} message={feedback.text} onClose={onClose} duration={5000} />
    </div>
  );
}


/** The form card of the supplier forms: title bar, then sections separated by headed groups. */
export function FormCard({ title, children }: { title: string; children: ReactNode }) {
  return (
    <div className="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
      <div className="border-b border-gray-200 px-6 py-5 dark:border-gray-800">
        <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">{title}</h3>
      </div>
      <div className="space-y-8 p-6">{children}</div>
    </div>
  );
}

export function FormSection({ title, aside, children }: { title: string; aside?: ReactNode; children: ReactNode }) {
  return (
    <div>
      <div className="mb-4 flex items-center justify-between gap-3 border-b border-gray-100 pb-2 dark:border-gray-800">
        <h4 className="text-base font-semibold text-gray-800 dark:text-white/90">{title}</h4>
        {aside}
      </div>
      {children}
    </div>
  );
}

