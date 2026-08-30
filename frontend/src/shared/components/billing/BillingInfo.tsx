import React from "react";

export default function BillingInfo() {
  return (
    <div className="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03] flex flex-col h-full">
      <div className="border-b border-gray-200 px-6 py-5 dark:border-gray-800">
        <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
          Billing Info
        </h3>
      </div>
      <div className="p-6 flex flex-col flex-1">
        <ul className="divide-y divide-gray-100 dark:divide-gray-800">
          <li className="flex py-4">
            <span className="w-1/3 text-sm text-gray-500 dark:text-gray-400">
              Name
            </span>
            <span className="w-2/3 text-sm font-medium text-gray-800 dark:text-white/90">
              Mushafrof Chowdhury
            </span>
          </li>
          <li className="flex py-4">
            <span className="w-1/3 text-sm text-gray-500 dark:text-gray-400">
              Street
            </span>
            <span className="w-2/3 text-sm font-medium text-gray-800 dark:text-white/90">
              800 E Elcamino Real, suite #400
            </span>
          </li>
          <li className="flex py-4">
            <span className="w-1/3 text-sm text-gray-500 dark:text-gray-400">
              City/State
            </span>
            <span className="w-2/3 text-sm font-medium text-gray-800 dark:text-white/90">
              Mountain View, CA, 94040
            </span>
          </li>
          <li className="flex py-4">
            <span className="w-1/3 text-sm text-gray-500 dark:text-gray-400">
              Country
            </span>
            <span className="w-2/3 text-sm font-medium text-gray-800 dark:text-white/90">
              United States of America
            </span>
          </li>
          <li className="flex py-4">
            <span className="w-1/3 text-sm text-gray-500 dark:text-gray-400">
              Zip/Postal code
            </span>
            <span className="w-2/3 text-sm font-medium text-gray-800 dark:text-white/90">
              19029
            </span>
          </li>
          <li className="flex py-4">
            <span className="w-1/3 text-sm text-gray-500 dark:text-gray-400">
              Town/City
            </span>
            <span className="w-2/3 text-sm font-medium text-gray-800 dark:text-white/90">
              New York
            </span>
          </li>
          <li className="flex py-4">
            <span className="w-1/3 text-sm text-gray-500 dark:text-gray-400">
              VAT Number
            </span>
            <span className="w-2/3 text-sm font-medium text-gray-800 dark:text-white/90">
              DE4920348
            </span>
          </li>
        </ul>

        <div className="mt-auto pt-6">
          <button className="flex w-full items-center justify-center gap-2 rounded-lg border border-gray-200 px-4 py-3 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-300 dark:hover:bg-gray-800 transition-colors">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
            </svg>
            Update Billing Address
          </button>
        </div>
      </div>
    </div>
  );
}
