import React, { useState } from "react";
import { CheckLineIcon } from "../../../icons";

export default function PricingTableOne() {
  const [isAnnual, setIsAnnual] = useState(false);

  return (
    <div className="w-full">
      <div className="mb-14 text-center">
        <h2 className="mb-6 text-3xl font-bold text-gray-900 dark:text-white/90 sm:text-[32px] leading-tight">
          Flexible Plans Tailored to Fit<br />Your Unique Needs!
        </h2>
        <div className="inline-flex items-center rounded-full bg-gray-100 p-1.5 dark:bg-gray-800">
          <button
            onClick={() => setIsAnnual(false)}
            className={`rounded-full px-6 py-2 text-sm font-medium transition-colors ${
              !isAnnual
                ? "bg-white text-gray-900 shadow-sm dark:bg-gray-700 dark:text-white"
                : "text-gray-500 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white"
            }`}
          >
            Monthly
          </button>
          <button
            onClick={() => setIsAnnual(true)}
            className={`rounded-full px-6 py-2 text-sm font-medium transition-colors ${
              isAnnual
                ? "bg-white text-gray-900 shadow-sm dark:bg-gray-700 dark:text-white"
                : "text-gray-500 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white"
            }`}
          >
            Annually
          </button>
        </div>
      </div>

      <div className="mx-auto w-full max-w-[1133px] grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {/* Starter Plan */}
        <div className="flex h-full flex-col rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900">
          <h3 className="mb-4 text-2xl font-bold text-gray-900 dark:text-white/90">Starter</h3>
          <div className="mb-2 flex items-center justify-between">
            <div className="flex items-baseline gap-1">
              <span className="text-[36px] font-bold leading-none text-gray-900 dark:text-white">$5.00</span>
              <span className="text-sm font-medium text-gray-500 dark:text-gray-400">/month</span>
            </div>
            <span className="text-base font-semibold text-gray-400 line-through dark:text-gray-500">$12.00</span>
          </div>
          <p className="mb-8 text-sm text-gray-500 dark:text-gray-400">For solo designers & freelancers</p>
          <div className="mb-8 h-px w-full bg-gray-100 dark:bg-gray-800"></div>
          <ul className="mb-10 flex flex-grow flex-col gap-4">
            {["5 website", "500 MB Storage", "Unlimited Sub-Domain", "3 Custom Domain", "Free SSL Certificate", "Unlimited Traffic"].map((feature, i) => (
              <li key={i} className="flex items-center gap-3 text-sm text-gray-600 dark:text-gray-400">
                <CheckLineIcon className="size-5 text-green-500" />
                {feature}
              </li>
            ))}
          </ul>
          <button className="mt-auto w-full rounded-lg bg-gray-900 py-3.5 text-center text-sm font-medium text-white hover:bg-gray-800 dark:bg-white dark:text-gray-900 dark:hover:bg-gray-100">
            Choose Starter
          </button>
        </div>

        {/* Medium Plan */}
        <div className="flex h-full flex-col rounded-2xl bg-[#1C2434] p-6 shadow-lg dark:bg-gray-800">
          <h3 className="mb-4 text-2xl font-bold text-white">Medium</h3>
          <div className="mb-2 flex items-center justify-between">
            <div className="flex items-baseline gap-1">
              <span className="text-[36px] font-bold leading-none text-white">$10.99</span>
              <span className="text-sm font-medium text-gray-300">/month</span>
            </div>
            <span className="text-base font-semibold text-gray-400 line-through">$30.00</span>
          </div>
          <p className="mb-8 text-sm text-gray-300">For working on commercial projects</p>
          <div className="mb-8 h-px w-full bg-gray-700 dark:bg-gray-700"></div>
          <ul className="mb-10 flex flex-grow flex-col gap-4">
            {["10 website", "1 GB Storage", "Unlimited Sub-Domain", "5 Custom Domain", "Free SSL Certificate", "Unlimited Traffic"].map((feature, i) => (
              <li key={i} className="flex items-center gap-3 text-sm text-gray-300">
                <CheckLineIcon className="size-5 text-green-500" />
                {feature}
              </li>
            ))}
          </ul>
          <button className="mt-auto w-full rounded-lg bg-brand-500 py-3.5 text-center text-sm font-medium text-white hover:bg-brand-600">
            Choose Starter
          </button>
        </div>

        {/* Large Plan */}
        <div className="flex h-full flex-col rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900">
          <h3 className="mb-4 text-2xl font-bold text-gray-900 dark:text-white/90">Large</h3>
          <div className="mb-2 flex items-center justify-between">
            <div className="flex items-baseline gap-1">
              <span className="text-[36px] font-bold leading-none text-gray-900 dark:text-white">$15.00</span>
              <span className="text-sm font-medium text-gray-500 dark:text-gray-400">/month</span>
            </div>
            <span className="text-base font-semibold text-gray-400 line-through dark:text-gray-500">$59.00</span>
          </div>
          <p className="mb-8 text-sm text-gray-500 dark:text-gray-400">For teams larger than 5 members</p>
          <div className="mb-8 h-px w-full bg-gray-100 dark:bg-gray-800"></div>
          <ul className="mb-10 flex flex-grow flex-col gap-4">
            {["15 website", "10 GB Storage", "Unlimited Sub-Domain", "10 Custom Domain", "Free SSL Certificate", "Unlimited Traffic"].map((feature, i) => (
              <li key={i} className="flex items-center gap-3 text-sm text-gray-600 dark:text-gray-400">
                <CheckLineIcon className="size-5 text-green-500" />
                {feature}
              </li>
            ))}
          </ul>
          <button className="mt-auto w-full rounded-lg bg-gray-900 py-3.5 text-center text-sm font-medium text-white hover:bg-gray-800 dark:bg-white dark:text-gray-900 dark:hover:bg-gray-100">
            Choose Starter
          </button>
        </div>
      </div>
    </div>
  );
}
