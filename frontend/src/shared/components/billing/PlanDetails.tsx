import React from "react";

export default function PlanDetails() {
  return (
    <div className="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
      <div className="border-b border-gray-200 px-6 py-5 dark:border-gray-800">
        <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
          Plan Details
        </h3>
      </div>
      <div className="p-6">
        <div className="grid grid-cols-1 gap-8 lg:grid-cols-2">
          {/* Left Column - Plan Info */}
          <div className="rounded-xl border border-gray-200 dark:border-gray-800">
            <div className="divide-y divide-gray-200 dark:divide-gray-800">
              <div className="p-5">
                <p className="text-sm text-gray-500 dark:text-gray-400 mb-1">
                  Current Plan
                </p>
                <p className="text-base font-medium text-gray-800 dark:text-white/90">
                  Professional
                </p>
              </div>
              <div className="p-5">
                <p className="text-sm text-gray-500 dark:text-gray-400 mb-1">
                  Monthly Limits
                </p>
                <p className="text-base font-medium text-gray-800 dark:text-white/90">
                  25,000 Orders
                </p>
              </div>
              <div className="p-5">
                <p className="text-sm text-gray-500 dark:text-gray-400 mb-1">
                  Cost
                </p>
                <p className="text-base font-medium text-gray-800 dark:text-white/90">
                  $199.00/month
                </p>
              </div>
              <div className="p-5">
                <p className="text-sm text-gray-500 dark:text-gray-400 mb-1">
                  Renewal Date
                </p>
                <p className="text-base font-medium text-gray-800 dark:text-white/90">
                  Mar 22, 2028
                </p>
              </div>
            </div>
            <div className="border-t border-gray-200 bg-gray-50 p-5 dark:border-gray-800 dark:bg-gray-900 rounded-b-xl">
              <div className="mb-3 flex justify-between items-end text-sm">
                <span className="font-medium text-gray-800 dark:text-white/90">Orders</span>
                <span className="text-gray-500 dark:text-gray-400">15,299 of 25,500 orders used</span>
              </div>
              <div className="h-2 w-full overflow-hidden rounded-full bg-gray-200 dark:bg-gray-800">
                <div
                  className="h-full rounded-full bg-brand-500"
                  style={{ width: "60%" }}
                ></div>
              </div>
            </div>
          </div>

          {/* Right Column - Benefits & Actions */}
          <div className="flex flex-col">
            <h4 className="mb-5 text-base font-semibold text-gray-800 dark:text-white/90">
              Plan Benefits
            </h4>
            <ul className="mb-8 flex flex-col gap-4 text-sm text-gray-500 dark:text-gray-400 font-medium">
              <li className="flex items-center gap-3">
                <svg className="w-5 h-5 text-gray-800 dark:text-white/90" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
                25,500 orders per month
              </li>
              <li className="flex items-center gap-3">
                <svg className="w-5 h-5 text-gray-800 dark:text-white/90" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
                Unlimited integrations
              </li>
              <li className="flex items-center gap-3">
                <svg className="w-5 h-5 text-gray-800 dark:text-white/90" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
                Exclusive AutoFile discount
              </li>
              <li className="flex items-center gap-3">
                <svg className="w-5 h-5 text-gray-800 dark:text-white/90" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
                10 GB Storage
              </li>
              <li className="flex items-center gap-3 text-gray-400 dark:text-gray-600">
                <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
                Custom Templates
              </li>
              <li className="flex items-center gap-3 text-gray-400 dark:text-gray-600">
                <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
                Advanced Marketing tool
              </li>
            </ul>

            <div className="mt-auto flex gap-4">
              <button className="rounded-lg border border-gray-200 px-6 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-300 dark:hover:bg-gray-800 transition-colors">
                Cancel Subscription
              </button>
              <button className="rounded-lg bg-brand-500 px-6 py-2.5 text-sm font-medium text-white hover:bg-brand-600 transition-colors">
                Upgrade to Pro
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
