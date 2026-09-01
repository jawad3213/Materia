import React from "react";
import PageMeta from "../../common/PageMeta";
import PageBreadcrumb from "../../common/PageBreadCrumb";
import ComponentCard from "../../common/ComponentCard";

export default function Notifications() {
  return (
    <>
      <PageMeta
        title="React.js Notifications Dashboard | TailAdmin - React.js Admin Dashboard Template"
        description="This is React.js Notifications page for TailAdmin - React.js Tailwind CSS Admin Dashboard Template"
      />
      <PageBreadcrumb pageTitle="Notifications" />

      <div className="space-y-6">
        {/* Announcement Bar */}
        <ComponentCard title="Announcement Bar">
          <div className="w-full rounded-lg border border-gray-200 bg-white p-4 shadow-theme-sm dark:border-gray-800 dark:bg-gray-900 sm:w-fit sm:p-5 sm:pr-8 xl:pr-16">
            <div className="flex flex-col gap-5 sm:flex-row sm:items-center sm:justify-between">
              <div className="flex items-start gap-4">
                <div className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full bg-brand-50 text-brand-500 dark:bg-brand-500/10">
                  <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                  </svg>
                </div>
                <div>
                  <h5 className="text-sm font-semibold text-gray-800 dark:text-white/90">
                    New update! Available
                  </h5>
                  <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                    Enjoy improved functionality and enhancements.
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <button className="rounded-lg border border-gray-200 bg-transparent px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-gray-800">
                  Later
                </button>
                <button className="rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600">
                  Update Now
                </button>
              </div>
            </div>
          </div>
        </ComponentCard>

        {/* Toast Notification */}
        <ComponentCard title="Toast Notification">
          <div className="relative rounded-lg border border-gray-200 bg-white p-5 shadow-theme-sm dark:border-gray-800 dark:bg-gray-900 sm:max-w-lg">
            <button className="absolute right-4 top-4 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300">
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
            <div className="mb-5 pr-6 text-sm text-gray-600 dark:text-gray-400 leading-relaxed">
              By Clicking on 'Accept', you agree to the storing of cookies on your device to enhance site navigation, analyze site usage, and assist in our marketing efforts.
            </div>
            <div className="flex items-center justify-end gap-3">
              <button className="text-sm font-medium text-gray-600 hover:text-gray-800 dark:text-gray-400 dark:hover:text-gray-200">
                Cookie Settings
              </button>
              <button className="rounded-lg border border-gray-200 bg-transparent px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-gray-800">
                Deny All
              </button>
              <button className="rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600">
                Accept All
              </button>
            </div>
          </div>
        </ComponentCard>

        {/* Success Notification */}
        <ComponentCard title="Success Notification">
          <div className="relative overflow-hidden rounded-lg border border-gray-200 bg-white p-4 shadow-theme-sm dark:border-gray-800 dark:bg-gray-900 sm:max-w-sm">
            <div className="absolute bottom-0 left-0 h-1 w-full bg-success-500"></div>
            <button className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300">
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
            <div className="flex items-center gap-4 pr-6">
              <div className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full bg-success-50 text-success-500 dark:bg-success-500/10">
                <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <h5 className="text-sm font-medium text-gray-800 dark:text-white/90">
                Success! Action Completed!
              </h5>
            </div>
          </div>
        </ComponentCard>

        {/* Info Notification */}
        <ComponentCard title="Info Notification">
          <div className="relative overflow-hidden rounded-lg border border-gray-200 bg-white p-4 shadow-theme-sm dark:border-gray-800 dark:bg-gray-900 sm:max-w-sm">
            <div className="absolute bottom-0 left-0 h-1 w-full bg-blue-500"></div>
            <button className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300">
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
            <div className="flex items-center gap-4 pr-6">
              <div className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full bg-blue-50 text-blue-500 dark:bg-blue-500/10">
                <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h5 className="text-sm font-medium text-gray-800 dark:text-white/90">
                Heads Up! New Information
              </h5>
            </div>
          </div>
        </ComponentCard>

        {/* Warning Notification */}
        <ComponentCard title="Warning Notification">
          <div className="relative overflow-hidden rounded-lg border border-gray-200 bg-white p-4 shadow-theme-sm dark:border-gray-800 dark:bg-gray-900 sm:max-w-sm">
            <div className="absolute bottom-0 left-0 h-1 w-full bg-warning-500"></div>
            <button className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300">
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
            <div className="flex items-center gap-4 pr-6">
              <div className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full bg-warning-50 text-warning-500 dark:bg-warning-500/10">
                <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
              </div>
              <h5 className="text-sm font-medium text-gray-800 dark:text-white/90">
                Alert: Double Check Required
              </h5>
            </div>
          </div>
        </ComponentCard>

        {/* Error Notification */}
        <ComponentCard title="Error Notification">
          <div className="relative overflow-hidden rounded-lg border border-gray-200 bg-white p-4 shadow-theme-sm dark:border-gray-800 dark:bg-gray-900 sm:max-w-sm">
            <div className="absolute bottom-0 left-0 h-1 w-full bg-error-500"></div>
            <button className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300">
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
            <div className="flex items-center gap-4 pr-6">
              <div className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full bg-error-50 text-error-500 dark:bg-error-500/10">
                <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h5 className="text-sm font-medium text-gray-800 dark:text-white/90">
                Something Went Wrong
              </h5>
            </div>
          </div>
        </ComponentCard>
      </div>
    </>
  );
}
