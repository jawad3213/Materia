import React from "react";
import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";

export default function UserGrowthChart() {
  const options: ApexOptions = {
    colors: ["#17C964"], // green
    chart: {
      fontFamily: "Outfit, sans-serif",
      type: "area",
      height: 60,
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
            return "User Growth";
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
      name: "User Growth",
      data: [10, 15, 20, 18, 25, 30, 28, 35, 40, 45],
    },
  ];

  return (
    <div className="p-6 bg-white border border-gray-200 rounded-2xl dark:bg-white/[0.03] dark:border-gray-800">
      <div className="flex justify-between items-end">
        <div>
          <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90 mb-1">
            User Growth
          </h3>
          <p className="text-sm text-gray-500 dark:text-gray-400 mb-6">
            New signups website + mobile
          </p>
          <h4 className="text-2xl font-bold text-gray-800 dark:text-white/90 mb-1">
            3,768
          </h4>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            <span className="text-success-500">+3.85%</span> than last Week
          </p>
        </div>
        <div className="w-32">
          <Chart options={options} series={series} type="area" height={60} />
        </div>
      </div>
    </div>
  );
}
