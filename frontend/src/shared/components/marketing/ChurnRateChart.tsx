import React from "react";
import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";

export default function ChurnRateChart() {
  const options: ApexOptions = {
    colors: ["#F6465D"],
    chart: {
      fontFamily: "Outfit, sans-serif",
      type: "line",
      height: 60,
      sparkline: {
        enabled: true,
      },
    },
    stroke: {
      curve: "smooth",
      width: 2,
    },
    tooltip: {
      fixed: {
        enabled: false,
      },
      x: {
        show: false,
      },
      y: {
        title: {
          formatter: function () {
            return "Churn Rate";
          },
        },
      },
      marker: {
        show: false,
      },
    },
  };

  const series = [
    {
      name: "Churn Rate",
      data: [10, 20, 15, 30, 20, 25, 10, 15, 20, 25],
    },
  ];

  return (
    <div className="p-6 bg-white border border-gray-200 rounded-2xl dark:bg-white/[0.03] dark:border-gray-800">
      <div className="flex justify-between items-end">
        <div>
          <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90 mb-1">
            Churn Rate
          </h3>
          <p className="text-sm text-gray-500 dark:text-gray-400 mb-6">
            Downgrade to Free plan
          </p>
          <h4 className="text-2xl font-bold text-gray-800 dark:text-white/90 mb-1">
            4.26%
          </h4>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            <span className="text-error-500">0.31%</span> than last Week
          </p>
        </div>
        <div className="w-32">
          <Chart options={options} series={series} type="line" height={60} />
        </div>
      </div>
    </div>
  );
}
