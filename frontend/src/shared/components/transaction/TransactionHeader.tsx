import React from "react";

export default function TransactionHeader() {
  return (
    <div className="flex flex-col gap-4 rounded-2xl border border-gray-200 bg-white px-6 py-5 sm:flex-row sm:items-center sm:justify-between dark:border-gray-800 dark:bg-white/[0.03]">
      <div className="flex flex-wrap items-center gap-4">
        <h4 className="text-base font-semibold text-gray-800 dark:text-white/90">
          Order ID : <span className="font-normal text-gray-500 dark:text-gray-400">#34834</span>
        </h4>
        <span className="inline-flex rounded-full bg-success-50 px-3 py-1 text-xs font-medium text-success-600 dark:bg-success-500/10 dark:text-success-500">
          Completed
        </span>
        <div className="hidden h-5 w-px bg-gray-200 sm:block dark:bg-gray-800"></div>
        <p className="text-sm text-gray-500 dark:text-gray-400">
          Due date: <span className="font-medium text-gray-800 dark:text-white/90">25 August 2025</span>
        </p>
      </div>

      <div className="flex items-center gap-3">
        <button className="rounded-lg bg-brand-500 px-6 py-2.5 text-sm font-medium text-white hover:bg-brand-600 transition-colors">
          View Receipt
        </button>
        <button className="rounded-lg border border-gray-200 px-6 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-300 dark:hover:bg-gray-800 transition-colors">
          Refund
        </button>
      </div>
    </div>
  );
}
