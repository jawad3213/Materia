import React from "react";
import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";

export default function ActiveUsersChart() {
  const options: ApexOptions = {
    colors: ["#465FFF"],
    chart: {
      fontFamily: "Outfit, sans-serif",
      type: "area",
      height: 180,
      sparkline: {
        enabled: true,
      },
    },
    stroke: {
      curve: "smooth",
      width: 2,
    },
    fill: {
      type: "gradient",
      gradient: {
        shadeIntensity: 1,
        opacityFrom: 0.4,
        opacityTo: 0,
        stops: [0, 90, 100],
      },
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
            return "Active Users";
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
      name: "Active Users",
      data: [10, 15, 13, 20, 25, 30, 45, 60, 50, 40],
    },
  ];

  return (
    <div className="p-6 bg-white border border-gray-200 rounded-2xl dark:bg-white/[0.03] dark:border-gray-800 flex flex-col justify-between h-full">
      <div className="flex justify-between items-start mb-6">
        <div>
          <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90 mb-4">
            Active Users
          </h3>
          <div className="flex items-center gap-2">
            <span className="relative flex h-3 w-3">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-error-500 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-3 w-3 bg-error-500 border-2 border-white dark:border-gray-900"></span>
            </span>
            <span className="text-3xl font-bold text-gray-800 dark:text-white">
              364
            </span>
            <span className="text-sm text-gray-500 dark:text-gray-400 mt-2">
              Live visitors
            </span>
          </div>
        </div>
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

      <div className="-ml-3 -mr-3 mb-6">
        <Chart options={options} series={series} type="area" height={180} />
      </div>

      <div className="flex items-center justify-between mt-auto">
        <div className="text-center">
          <h4 className="text-lg font-bold text-gray-800 dark:text-white">
            224
          </h4>
          <span className="text-xs text-gray-500 dark:text-gray-400">
            Avg, Daily
          </span>
        </div>
        <div className="h-10 w-px bg-gray-200 dark:bg-gray-800"></div>
        <div className="text-center">
          <h4 className="text-lg font-bold text-gray-800 dark:text-white">
            1.4K
          </h4>
          <span className="text-xs text-gray-500 dark:text-gray-400">
            Avg, Weekly
          </span>
        </div>
        <div className="h-10 w-px bg-gray-200 dark:bg-gray-800"></div>
        <div className="text-center">
          <h4 className="text-lg font-bold text-gray-800 dark:text-white">
            22.1K
          </h4>
          <span className="text-xs text-gray-500 dark:text-gray-400">
            Avg, Monthly
          </span>
        </div>
      </div>
    </div>
  );
}
