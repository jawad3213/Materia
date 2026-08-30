import React from "react";
import { CheckLineIcon, CloseLineIcon, UserIcon, BoxIcon, ShootingStarIcon } from "../../../icons";

export default function PricingTableTwo() {
  return (
    <div className="w-full">
      <div className="mx-auto w-full max-w-[1133px] grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {/* Personal Plan */}
        <div className="flex h-full flex-col rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900">
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-2xl font-bold text-gray-900 dark:text-white/90">Personal</h3>
            <div className="flex size-12 items-center justify-center rounded-xl bg-brand-50 text-brand-500 dark:bg-brand-500/10">
              <UserIcon className="size-6" />
            </div>
          </div>
          <div className="mb-2 flex items-baseline gap-1">
            <span className="text-[36px] font-bold leading-none text-gray-900 dark:text-white">$59.00</span>
            <span className="text-sm font-medium text-gray-500 dark:text-gray-400">/ Lifetime</span>
          </div>
          <p className="mb-8 text-sm text-gray-500 dark:text-gray-400">For solo designers & freelancers</p>
          <div className="mb-8 h-px w-full bg-gray-100 dark:bg-gray-800"></div>
          <ul className="mb-10 flex flex-grow flex-col gap-4">
            {["5 website", "500 MB Storage", "Unlimited Sub-Domain", "3 Custom Domain"].map((feature, i) => (
              <li key={i} className="flex items-center gap-3 text-sm text-gray-600 dark:text-gray-400">
                <CheckLineIcon className="size-5 text-green-500" />
                {feature}
              </li>
            ))}
            {["Free SSL Certificate", "Unlimited Traffic"].map((feature, i) => (
              <li key={`close-${i}`} className="flex items-center gap-3 text-sm text-gray-400 dark:text-gray-500">
                <CloseLineIcon className="size-5 text-gray-400 dark:text-gray-500" />
                {feature}
              </li>
            ))}
          </ul>
          <button className="mt-auto w-full rounded-lg bg-gray-900 py-3.5 text-center text-sm font-medium text-white hover:bg-gray-800 dark:bg-white dark:text-gray-900 dark:hover:bg-gray-100">
            Choose Starter
          </button>
        </div>

        {/* Professional Plan */}
        <div className="flex h-full flex-col rounded-2xl border border-brand-500 bg-white p-6 shadow-lg dark:bg-gray-900 relative">
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-2xl font-bold text-gray-900 dark:text-white/90">Professional</h3>
            <div className="flex size-12 items-center justify-center rounded-xl bg-brand-50 text-brand-500 dark:bg-brand-500/10">
              <BoxIcon className="size-6" />
            </div>
          </div>
          <div className="mb-2 flex items-baseline gap-1">
            <span className="text-[36px] font-bold leading-none text-gray-900 dark:text-white">$199.00</span>
            <span className="text-sm font-medium text-gray-500 dark:text-gray-400">/ Lifetime</span>
          </div>
          <p className="mb-8 text-sm text-gray-500 dark:text-gray-400">For working on commercial projects</p>
          <div className="mb-8 h-px w-full bg-gray-100 dark:bg-gray-800"></div>
          <ul className="mb-10 flex flex-grow flex-col gap-4">
            {["10 website", "1GB Storage", "Unlimited Sub-Domain", "5 Custom Domain", "Free SSL Certificate"].map((feature, i) => (
              <li key={i} className="flex items-center gap-3 text-sm text-gray-600 dark:text-gray-400">
                <CheckLineIcon className="size-5 text-green-500" />
                {feature}
              </li>
            ))}
            {["Unlimited Traffic"].map((feature, i) => (
              <li key={`close-${i}`} className="flex items-center gap-3 text-sm text-gray-400 dark:text-gray-500">
                <CloseLineIcon className="size-5 text-gray-400 dark:text-gray-500" />
                {feature}
              </li>
            ))}
          </ul>
          <button className="mt-auto w-full rounded-lg bg-brand-500 py-3.5 text-center text-sm font-medium text-white hover:bg-brand-600">
            Choose This Plan
          </button>
        </div>

        {/* Enterprise Plan */}
        <div className="flex h-full flex-col rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900">
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-2xl font-bold text-gray-900 dark:text-white/90">Enterprise</h3>
            <div className="flex size-12 items-center justify-center rounded-xl bg-brand-50 text-brand-500 dark:bg-brand-500/10">
              <ShootingStarIcon className="size-6" />
            </div>
          </div>
          <div className="mb-2 flex items-baseline gap-1">
            <span className="text-[36px] font-bold leading-none text-gray-900 dark:text-white">$599.00</span>
            <span className="text-sm font-medium text-gray-500 dark:text-gray-400">/ Lifetime</span>
          </div>
          <p className="mb-8 text-sm text-gray-500 dark:text-gray-400">For teams larger than 5 members</p>
          <div className="mb-8 h-px w-full bg-gray-100 dark:bg-gray-800"></div>
          <ul className="mb-10 flex flex-grow flex-col gap-4">
            {["15 website", "10GB Storage", "Unlimited Sub-Domain", "10 Custom Domain", "Free SSL Certificate", "Unlimited Traffic"].map((feature, i) => (
              <li key={i} className="flex items-center gap-3 text-sm text-gray-600 dark:text-gray-400">
                <CheckLineIcon className="size-5 text-green-500" />
                {feature}
              </li>
            ))}
          </ul>
          <button className="mt-auto w-full rounded-lg bg-gray-900 py-3.5 text-center text-sm font-medium text-white hover:bg-gray-800 dark:bg-white dark:text-gray-900 dark:hover:bg-gray-100">
            Choose This Plan
          </button>
        </div>
      </div>
    </div>
  );
}
