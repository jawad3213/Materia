import React from "react";

export default function InvoiceOverview() {
  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
          Overview
        </h3>
        <button className="flex items-center justify-center gap-2 rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 transition-colors">
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          Create an Invoice
        </button>
      </div>

      <div className="rounded-xl border border-gray-200 dark:border-gray-800">
        <div className="grid grid-cols-1 divide-y divide-gray-200 sm:grid-cols-2 sm:divide-y-0 sm:divide-x lg:grid-cols-4 dark:divide-gray-800">
          <div className="p-6">
            <p className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">
              Overdue
            </p>
            <h4 className="text-2xl font-semibold text-gray-800 dark:text-white/90">
              $120.80
            </h4>
          </div>
          <div className="p-6">
            <p className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">
              Due within next 30 days
            </p>
            <h4 className="text-2xl font-semibold text-gray-800 dark:text-white/90">
              0.00
            </h4>
          </div>
          <div className="p-6">
            <p className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">
              Average time to get paid
            </p>
            <h4 className="text-2xl font-semibold text-gray-800 dark:text-white/90">
              24 days
            </h4>
          </div>
          <div className="p-6">
            <p className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">
              Upcoming Payout
            </p>
            <h4 className="text-2xl font-semibold text-gray-800 dark:text-white/90">
              $3,450.50
            </h4>
          </div>
        </div>
      </div>
    </div>
  );
}
