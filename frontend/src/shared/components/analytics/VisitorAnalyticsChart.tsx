import React from "react";
import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";

export default function VisitorAnalyticsChart() {
  const options: ApexOptions = {
    colors: ["#465FFF"],
    chart: {
      fontFamily: "Outfit, sans-serif",
      type: "bar",
      height: 350,
      toolbar: {
        show: false,
      },
    },
    plotOptions: {
      bar: {
        horizontal: false,
        columnWidth: "35%",
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
      categories: Array.from({ length: 30 }, (_, i) => i + 1),
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
    tooltip: {
      y: {
        formatter: function (val) {
          return val + " Visitors";
        },
      },
    },
  };

  const series = [
    {
      name: "Visitors",
      data: [
        160, 380, 195, 290, 180, 190, 285, 105, 210, 385, 275, 110, 120, 205,
        265, 185, 305, 110, 85, 375, 105, 215, 285, 165, 285, 105, 110, 285,
        375, 305,
      ],
    },
  ];

  return (
    <div className="p-6 bg-white border border-gray-200 rounded-2xl dark:bg-white/[0.03] dark:border-gray-800">
      <div className="flex flex-col gap-4 mb-6 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
            Analytics
          </h3>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Visitor analytics of last 30 days
          </p>
        </div>
        <div className="flex bg-gray-50 dark:bg-gray-900 rounded-lg p-1 border border-gray-200 dark:border-gray-800 w-max">
          <button className="px-4 py-1.5 text-sm font-medium rounded-md bg-white dark:bg-gray-800 text-gray-800 dark:text-white shadow-sm transition-all">
            Monthly
          </button>
          <button className="px-4 py-1.5 text-sm font-medium rounded-md text-gray-500 hover:text-gray-800 dark:text-gray-400 dark:hover:text-white transition-all">
            Quarterly
          </button>
          <button className="px-4 py-1.5 text-sm font-medium rounded-md text-gray-500 hover:text-gray-800 dark:text-gray-400 dark:hover:text-white transition-all">
            Annually
          </button>
        </div>
      </div>
      <div id="chartOne" className="-ml-3 w-full">
        <Chart options={options} series={series} type="bar" height={350} />
      </div>
    </div>
  );
}
