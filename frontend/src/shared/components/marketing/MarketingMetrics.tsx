import React from "react";

const metrics = [
  {
    title: "Total Revenue",
    value: "$200,45.87",
    change: "+2.5%",
    status: "success",
  },
  {
    title: "Active Users",
    value: "9,528",
    change: "+9.5%",
    status: "success",
  },
  {
    title: "Customer Lifetime Value",
    value: "$849.54",
    change: "-1.6%",
    status: "error",
  },
  {
    title: "Customer Acquisition Cost",
    value: "9,528",
    change: "+3.5%",
    status: "success",
  },
];

export default function MarketingMetrics() {
  return (
    <div>
      <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90 mb-4">
        Overview
      </h3>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 divide-y sm:divide-y-0 sm:divide-x divide-gray-100 dark:divide-gray-800 bg-white border border-gray-200 dark:bg-white/[0.03] dark:border-gray-800 rounded-2xl">
        {metrics.map((metric, index) => (
          <div
            key={index}
            className="flex flex-col justify-center p-6"
          >
            <p className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-2">
              {metric.title}
            </p>
            <div className="flex items-center gap-3">
              <h4 className="text-2xl font-bold text-gray-800 dark:text-white/90">
                {metric.value}
              </h4>
              <span
                className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${
                  metric.status === "success"
                    ? "bg-success-50 text-success-600 dark:bg-success-500/10 dark:text-success-500"
                    : "bg-error-50 text-error-600 dark:bg-error-500/10 dark:text-error-500"
                }`}
              >
                {metric.change}
              </span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
