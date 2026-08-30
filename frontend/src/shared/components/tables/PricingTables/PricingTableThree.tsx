import React from "react";
import { CheckLineIcon } from "../../../icons";

export default function PricingTableThree() {
  return (
    <div className="w-full">
      <div className="mx-auto w-full max-w-[1133px] grid gap-4 sm:gap-6 lg:grid-cols-4">
        {/* Personal Plan */}
        <div className="lg:pt-6 h-full">
          <div className="flex h-full flex-col pr-4">
            <h3 className="mb-2 text-2xl font-bold text-gray-900 dark:text-white/90">Personal</h3>
            <p className="mb-6 text-sm text-gray-500 dark:text-gray-400">Perfect plan for Starters</p>
            <div className="mb-2">
              <span className="text-[36px] font-bold leading-none text-gray-900 dark:text-white">Free</span>
            </div>
            <p className="mb-6 text-sm text-gray-500 dark:text-gray-400">For a Lifetime</p>
            
            <button className="mb-8 w-full rounded-lg border border-gray-200 bg-white py-3.5 text-center text-sm font-medium text-gray-500 hover:bg-gray-50 dark:border-gray-800 dark:bg-gray-900 dark:text-gray-400 dark:hover:bg-gray-800">
              Current Plan
            </button>

            <ul className="flex flex-col gap-3">
              {["Unlimited Projects", "Share with 5 team members", "Sync across devices"].map((feature, i) => (
                <li key={i} className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400 whitespace-nowrap overflow-hidden text-ellipsis">
                  <CheckLineIcon className="size-5 text-green-500" />
                  {feature}
                </li>
              ))}
            </ul>
          </div>
        </div>

        {/* Professional Plan */}
        <div className="lg:pt-6 h-full">
          <div className="flex h-full flex-col pr-4">
            <h3 className="mb-2 text-2xl font-bold text-gray-900 dark:text-white/90">Professional</h3>
            <p className="mb-6 text-sm text-gray-500 dark:text-gray-400">For users who want to do more</p>
            <div className="mb-2">
              <span className="text-[36px] font-bold leading-none text-gray-900 dark:text-white">$99.00</span>
            </div>
            <p className="mb-6 text-sm text-gray-500 dark:text-gray-400">/year</p>
            
            <button className="mb-8 w-full rounded-lg bg-brand-500 py-3.5 text-center text-sm font-medium text-white hover:bg-brand-600">
              Try for Free
            </button>

            <ul className="flex flex-col gap-3">
              {["Unlimited Projects", "Share with 5 team members", "Sync across devices", "30 days version history"].map((feature, i) => (
                <li key={i} className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400 whitespace-nowrap overflow-hidden text-ellipsis">
                  <CheckLineIcon className="size-5 text-green-500" />
                  {feature}
                </li>
              ))}
            </ul>
          </div>
        </div>

        {/* Team Plan (Highlighted) */}
        <div>
          <div className="relative flex flex-col rounded-2xl bg-brand-500 p-8 shadow-xl z-10">
            <div className="mb-2 flex items-center justify-between">
              <h3 className="text-2xl font-bold text-white">Team</h3>
              <span className="rounded-full bg-white/10 border border-white/20 px-3 py-1 text-[10px] font-medium text-white">
                Recommended
              </span>
            </div>
            <p className="mb-6 text-sm text-blue-100">Your entire team in one place</p>
            <div className="mb-2">
              <span className="text-[36px] font-bold leading-none text-white">$299</span>
            </div>
            <p className="mb-6 text-sm text-blue-100">/year</p>
            
            <button className="mb-8 w-full rounded-lg bg-white py-3.5 text-center text-sm font-medium text-brand-500 hover:bg-gray-50">
              Try for Free
            </button>

            <ul className="flex flex-col gap-3">
              {["Unlimited Projects", "Share with 5 team members", "Sync across devices", "Sharing permissions", "Admin tools"].map((feature, i) => (
                <li key={i} className="flex items-center gap-2 text-sm text-white whitespace-nowrap overflow-hidden text-ellipsis">
                  <CheckLineIcon className="size-5 text-white" />
                  {feature}
                </li>
              ))}
            </ul>
          </div>
        </div>

        {/* Enterprise Plan */}
        <div className="lg:pt-6 h-full">
          <div className="flex h-full flex-col pl-4">
            <h3 className="mb-2 text-2xl font-bold text-gray-900 dark:text-white/90">Enterprise</h3>
            <p className="mb-6 text-sm text-gray-500 dark:text-gray-400">Run your company on your terms</p>
            <div className="mb-2">
              <span className="text-[36px] font-bold leading-none text-gray-900 dark:text-white">Custom</span>
            </div>
            <p className="mb-6 text-sm text-gray-500 dark:text-gray-400">Reach out for a quote</p>
            
            <button className="mb-8 w-full rounded-lg bg-brand-500 py-3.5 text-center text-sm font-medium text-white hover:bg-brand-600">
              Try for Free
            </button>

            <ul className="flex flex-col gap-3">
              {["Unlimited Projects", "Share with 5 team members", "Sync across devices", "Sharing permissions", "User provisioning (SCIM)", "Advanced security"].map((feature, i) => (
                <li key={i} className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400 whitespace-nowrap overflow-hidden text-ellipsis">
                  <CheckLineIcon className="size-5 text-green-500" />
                  {feature}
                </li>
              ))}
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}
