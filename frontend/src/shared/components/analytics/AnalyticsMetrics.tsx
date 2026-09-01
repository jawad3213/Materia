import React from "react";
import Badge from "../ui/badge/Badge";

const metrics = [
  {
    title: "Unique Visitors",
    value: "24.7K",
    change: "+20%",
    status: "success",
  },
  {
    title: "Total Pageviews",
    value: "55.9K",
    change: "+4%",
    status: "success",
  },
  {
    title: "Bounce Rate",
    value: "54%",
    change: "-1.59%",
    status: "error",
  },
  {
    title: "Visit Duration",
    value: "2m 56s",
    change: "+7%",
    status: "success",
  },
];

export default function AnalyticsMetrics() {
  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4 sm:gap-6">
      {metrics.map((metric, index) => (
        <div
          key={index}
          className="p-6 bg-white border border-gray-200 rounded-2xl dark:bg-white/[0.03] dark:border-gray-800"
        >
          <p className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-2">
            {metric.title}
          </p>
          <div className="flex items-end justify-between">
            <h4 className="text-3xl font-bold text-gray-800 dark:text-white/90">
              {metric.value}
            </h4>
            <div className="flex items-center gap-1.5">
              <span
                className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${
                  metric.status === "success"
                    ? "bg-success-50 text-success-600 dark:bg-success-500/10 dark:text-success-500"
                    : "bg-error-50 text-error-600 dark:bg-error-500/10 dark:text-error-500"
                }`}
              >
                {metric.change}
              </span>
              <span className="text-xs font-medium text-gray-500 dark:text-gray-400">
                Vs last month
              </span>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
