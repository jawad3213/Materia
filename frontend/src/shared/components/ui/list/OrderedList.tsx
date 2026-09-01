import React from "react";

export default function OrderedList() {
  return (
    <ol className="flex flex-col rounded-xl border border-gray-200 dark:border-gray-800">
      <li className="flex items-center gap-3 border-b border-gray-200 px-5 py-3 text-sm font-medium text-gray-700 last:border-b-0 dark:border-gray-800 dark:text-gray-300">
        1. Lorem ipsum dolor sit amet
      </li>
      <li className="flex items-center gap-3 border-b border-gray-200 px-5 py-3 text-sm font-medium text-gray-700 last:border-b-0 dark:border-gray-800 dark:text-gray-300">
        2. It is a long established fact reader
      </li>
      <li className="flex items-center gap-3 border-b border-gray-200 px-5 py-3 text-sm font-medium text-gray-700 last:border-b-0 dark:border-gray-800 dark:text-gray-300">
        3. Lorem ipsum dolor sit amet
      </li>
      <li className="flex items-center gap-3 border-b border-gray-200 px-5 py-3 text-sm font-medium text-gray-700 last:border-b-0 dark:border-gray-800 dark:text-gray-300">
        4. Lorem ipsum dolor sit amet
      </li>
      <li className="flex items-center gap-3 border-b border-gray-200 px-5 py-3 text-sm font-medium text-gray-700 last:border-b-0 dark:border-gray-800 dark:text-gray-300">
        5. Lorem ipsum dolor sit amet
      </li>
    </ol>
  );
}
