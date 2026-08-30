import React from "react";
import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";

export default function ProductPerformance() {
  const options: ApexOptions = {
    colors: ["#465FFF"],
    chart: {
      fontFamily: "Outfit, sans-serif",
      type: "bar",
      height: 180,
      toolbar: {
        show: false,
      },
      sparkline: {
        enabled: false,
      },
    },
    plotOptions: {
      bar: {
        horizontal: false,
        columnWidth: "30%",
        borderRadius: 4,
      },
    },
    dataLabels: {
      enabled: false,
    },
    stroke: {
      show: true,
      width: 4,
      colors: ["transparent"],
    },
    xaxis: {
      categories: ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"],
      axisBorder: {
        show: false,
      },
      axisTicks: {
        show: false,
      },
      labels: {
        style: {
          colors: "#64748B",
        },
      },
    },
    yaxis: {
      min: 0,
      max: 400,
      tickAmount: 4,
      labels: {
        style: {
          colors: "#64748B",
        },
      },
    },
    grid: {
      borderColor: "#E2E8F0",
      strokeDashArray: 4,
      yaxis: {
        lines: {
          show: true,
        },
      },
    },
    fill: {
      opacity: 1,
    },
  };

  const series = [
    {
      name: "Sales",
      data: [160, 380, 195, 290, 180, 190, 150],
    },
  ];

  return (
    <div className="p-6 bg-white border border-gray-200 rounded-2xl dark:bg-white/[0.03] dark:border-gray-800">
      <div className="flex justify-between items-center mb-6">
        <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
          Product Performance
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

      <div className="inline-flex bg-gray-50 dark:bg-gray-900 rounded-lg p-1 border border-gray-200 dark:border-gray-800 mb-6 w-full sm:w-auto">
        <button className="px-4 py-2 text-sm font-medium rounded-md bg-white dark:bg-gray-800 text-gray-800 dark:text-white shadow-sm transition-all text-center">
          Daily Sales
        </button>
        <button className="px-4 py-2 text-sm font-medium rounded-md text-gray-500 hover:text-gray-800 dark:text-gray-400 dark:hover:text-white transition-all text-center">
          Online Sales
        </button>
        <button className="px-4 py-2 text-sm font-medium rounded-md text-gray-500 hover:text-gray-800 dark:text-gray-400 dark:hover:text-white transition-all text-center">
          New Users
        </button>
      </div>

      <div className="grid grid-cols-2 gap-4 mb-6">
        <div className="flex flex-col gap-1 items-center bg-gray-50 dark:bg-gray-900 rounded-lg p-4 border border-gray-100 dark:border-gray-800">
          <span className="text-sm text-gray-500 dark:text-gray-400 text-center">
            Digital Product
          </span>
          <div className="flex items-center gap-2">
            <span className="text-success-500">↑</span>
            <h4 className="text-xl font-bold text-gray-800 dark:text-white">
              790
            </h4>
          </div>
        </div>
        <div className="flex flex-col gap-1 items-center bg-gray-50 dark:bg-gray-900 rounded-lg p-4 border border-gray-100 dark:border-gray-800">
          <span className="text-sm text-gray-500 dark:text-gray-400 text-center">
            Physical Product
          </span>
          <div className="flex items-center gap-2">
            <span className="text-error-500">↓</span>
            <h4 className="text-xl font-bold text-gray-800 dark:text-white">
              572
            </h4>
          </div>
        </div>
      </div>

      <div className="p-4 border border-gray-100 dark:border-gray-800 rounded-xl">
        <div className="flex justify-between items-start mb-2">
          <div>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Average Daily Sales
            </p>
            <h4 className="text-2xl font-bold text-gray-800 dark:text-white mt-1">
              $2,950
            </h4>
          </div>
          <span className="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium bg-error-50 text-error-600 dark:bg-error-500/10 dark:text-error-500">
            ↓ 0.52%
          </span>
        </div>
        <div className="-ml-3 -mr-3">
          <Chart options={options} series={series} type="bar" height={180} />
        </div>
      </div>
    </div>
  );
}
