import React from "react";

const details = [
  { label: "Name", value: "Mushafrof Chowdhury" },
  { label: "Email", value: "name@example.com" },
  { label: "Phone", value: "Mountain View, CA, 94040" },
  { label: "Phone", value: "+123 456 7890" },
  { label: "Country", value: "United States" },
  { label: "Address", value: "62 Miles Drive St, Newark, NJ 07103, California." },
];

export default function TransactionCustomerDetails() {
  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">
      <h3 className="mb-5 text-lg font-semibold text-gray-800 dark:text-white/90">
        Customer Details
      </h3>
      <div className="flex flex-col">
        {details.map((item, index) => (
          <div
            key={index}
            className={`flex flex-col sm:flex-row sm:items-start py-3 ${
              index !== details.length - 1 ? "border-b border-gray-100 dark:border-gray-800" : ""
            }`}
          >
            <span className="mb-1 w-full max-w-[120px] text-sm text-gray-500 dark:text-gray-400 sm:mb-0 shrink-0">
              {item.label}
            </span>
            <span className="text-sm font-medium text-gray-800 dark:text-white/90">
              {item.value}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
