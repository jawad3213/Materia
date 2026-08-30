import React from "react";

export default function ListWithButton() {
  return (
    <ul className="flex flex-col rounded-xl border border-gray-200 dark:border-gray-800">
      <li className="border-b border-gray-200 last:border-b-0 dark:border-gray-800">
        <button className="flex w-full items-center gap-3 px-5 py-3 text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-brand-500 dark:text-gray-300 dark:hover:bg-white/5">
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="1.5">
            <path strokeLinecap="round" strokeLinejoin="round" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
          </svg>
          Inbox
        </button>
      </li>
      <li className="border-b border-gray-200 last:border-b-0 dark:border-gray-800">
        <button className="flex w-full items-center gap-3 px-5 py-3 text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-brand-500 dark:text-gray-300 dark:hover:bg-white/5">
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="1.5">
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
          </svg>
          Sent
        </button>
      </li>
      <li className="border-b border-gray-200 last:border-b-0 dark:border-gray-800">
        <button className="flex w-full items-center gap-3 px-5 py-3 text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-brand-500 dark:text-gray-300 dark:hover:bg-white/5">
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="1.5">
            <path strokeLinecap="round" strokeLinejoin="round" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
          </svg>
          Drafts
        </button>
      </li>
      <li className="border-b border-gray-200 last:border-b-0 dark:border-gray-800">
        <button className="flex w-full items-center gap-3 px-5 py-3 text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-brand-500 dark:text-gray-300 dark:hover:bg-white/5">
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="1.5">
            <path strokeLinecap="round" strokeLinejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
          </svg>
          Trash
        </button>
      </li>
      <li className="border-b border-gray-200 last:border-b-0 dark:border-gray-800">
        <button className="flex w-full items-center gap-3 px-5 py-3 text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-brand-500 dark:text-gray-300 dark:hover:bg-white/5">
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="1.5">
            <path strokeLinecap="round" strokeLinejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          Spam
        </button>
      </li>
    </ul>
  );
}
