import React from "react";

const pages = [
  { name: "tailadmin.com", pageviews: "4.7K" },
  { name: "preview.tailadmin.com", pageviews: "3.4K" },
  { name: "docs.tailadmin.com", pageviews: "2.9K" },
  { name: "tailadmin.com/components", pageviews: "1.5K" },
];

export default function TopPages() {
  return (
    <div className="p-6 bg-white border border-gray-200 rounded-2xl dark:bg-white/[0.03] dark:border-gray-800">
      <div className="flex justify-between items-center mb-6">
        <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
          Top Pages
        </h3>
        <button className="text-gray-400 hover:text-gray-800 dark:hover:text-white">
          <svg
            width="20"
            height="20"
            viewBox="0 0 20 20"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M10.0003 10.8333C10.4606 10.8333 10.8337 10.4602 10.8337 10C10.8337 9.53975 10.4606 9.16666 10.0003 9.16666C9.54009 9.16666 9.16699 9.53975 9.16699 10C9.16699 10.4602 9.54009 10.8333 10.0003 10.8333Z"
              stroke="currentColor"
              strokeWidth="1.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
            <path
              d="M10.0003 4.99999C10.4606 4.99999 10.8337 4.62689 10.8337 4.16666C10.8337 3.70642 10.4606 3.33333 10.0003 3.33333C9.54009 3.33333 9.16699 3.70642 9.16699 4.16666C9.16699 4.62689 9.54009 4.99999 10.0003 4.99999Z"
              stroke="currentColor"
              strokeWidth="1.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
            <path
              d="M10.0003 16.6667C10.4606 16.6667 10.8337 16.2936 10.8337 15.8333C10.8337 15.3731 10.4606 15 10.0003 15C9.54009 15 9.16699 15.3731 9.16699 15.8333C9.16699 16.2936 9.54009 16.6667 10.0003 16.6667Z"
              stroke="currentColor"
              strokeWidth="1.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </button>
      </div>

      <div className="flex flex-col mb-6">
        <div className="flex items-center justify-between py-2 border-b border-gray-100 dark:border-gray-800">
          <span className="text-xs font-medium text-gray-500 uppercase dark:text-gray-400">
            Source
          </span>
          <span className="text-xs font-medium text-gray-500 uppercase dark:text-gray-400">
            Pageview
          </span>
        </div>
        {pages.map((page, index) => (
          <div
            key={index}
            className="flex items-center justify-between py-3 border-b border-gray-100 dark:border-gray-800 last:border-none"
          >
            <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
              {page.name}
            </span>
            <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
              {page.pageviews}
            </span>
          </div>
        ))}
      </div>

      <button className="flex w-full items-center justify-center gap-2 rounded-lg border border-gray-200 bg-white px-4 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-800 dark:bg-transparent dark:text-gray-300 dark:hover:bg-white/[0.03] transition-all">
        Channels Report
        <svg
          width="16"
          height="16"
          viewBox="0 0 16 16"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M3.33333 8H12.6667"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
          <path
            d="M8 3.33334L12.6667 8.00001L8 12.6667"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      </button>
    </div>
  );
}
