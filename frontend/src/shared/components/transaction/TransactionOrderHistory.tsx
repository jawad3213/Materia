import React from "react";

const history = [
  {
    id: 1,
    title: "Checkout Started",
    description: "via tailadmin.com",
    time: "12:54",
    date: "12th Apr 28",
    icon: (
      <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
      </svg>
    ),
  },
  {
    id: 2,
    title: "Purchased",
    description: "for US$4,235 via PayPal",
    time: "12:58",
    date: "12th Apr 28",
    icon: (
      <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
      </svg>
    ),
  },
  {
    id: 3,
    title: "Receipt Email Sent",
    description: "Receipt #1734535",
    time: "12:58",
    date: "12th Apr 28",
    icon: (
      <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
      </svg>
    ),
  },
];

export default function TransactionOrderHistory() {
  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">
      <h3 className="mb-6 text-lg font-semibold text-gray-800 dark:text-white/90">
        Order History
      </h3>
      <div className="relative border-l border-dashed border-gray-200 ml-5 dark:border-gray-800">
        <div className="flex flex-col gap-6">
          {history.map((item, index) => (
            <div key={item.id} className="relative flex items-start pl-8">
              <div className="absolute -left-[20px] top-0 flex h-10 w-10 items-center justify-center rounded-full border border-gray-200 bg-white text-gray-500 dark:border-gray-800 dark:bg-gray-900 dark:text-gray-400">
                {item.icon}
              </div>
              <div className="flex flex-1 flex-col sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <h4 className="text-sm font-semibold text-gray-800 dark:text-white/90">
                    {item.title}
                  </h4>
                  <p className="mt-0.5 text-sm text-gray-500 dark:text-gray-400">
                    {item.description}
                  </p>
                </div>
                <div className="mt-2 text-right sm:mt-0">
                  <span className="block text-sm text-gray-500 dark:text-gray-400">
                    {item.time}
                  </span>
                  <span className="block text-xs text-gray-400 dark:text-gray-500">
                    {item.date}
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="mt-8 flex flex-wrap items-center gap-3 border-t border-gray-100 pt-6 dark:border-gray-800">
        <button className="rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-300 dark:hover:bg-gray-800 transition-colors">
          Resend
        </button>
        <button className="rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-300 dark:hover:bg-gray-800 transition-colors">
          Forward
        </button>
        <button className="rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-800 dark:text-gray-300 dark:hover:bg-gray-800 transition-colors">
          Preview
        </button>
      </div>
    </div>
  );
}
