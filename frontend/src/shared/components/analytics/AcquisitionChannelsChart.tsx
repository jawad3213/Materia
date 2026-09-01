import React from "react";
import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";

export default function AcquisitionChannelsChart() {
  const options: ApexOptions = {
    colors: ["#313D9C", "#465FFF", "#8EAAFB", "#C6D2FD"],
    chart: {
      fontFamily: "Outfit, sans-serif",
      type: "bar",
      height: 350,
      stacked: true,
      toolbar: {
        show: false,
      },
    },
    plotOptions: {
      bar: {
        horizontal: false,
        columnWidth: "40%",
        borderRadius: 8,
        borderRadiusApplication: "end",
        borderRadiusWhenStacked: "last",
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
      categories: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug"],
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
      max: 120,
      tickAmount: 6,
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
    legend: {
      position: "top",
      horizontalAlign: "left",
      fontFamily: "Outfit",
      markers: {
        shape: "circle" as any,
      },
    },
  };

  const series = [
    {
      name: "Direct",
      data: [44, 55, 41, 67, 22, 43, 55, 41],
    },
    {
      name: "Referral",
      data: [13, 23, 20, 8, 13, 27, 13, 23],
    },
    {
      name: "Organic Search",
      data: [11, 17, 15, 15, 21, 14, 11, 20],
    },
    {
      name: "Social",
      data: [21, 7, 25, 13, 22, 8, 25, 20],
    },
  ];

  return (
    <div className="p-6 bg-white border border-gray-200 rounded-2xl dark:bg-white/[0.03] dark:border-gray-800">
      <div className="flex justify-between items-center mb-4">
        <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
          Acquisition Channels
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
      <div className="-ml-3 -mr-3">
        <Chart options={options} series={series} type="bar" height={350} />
      </div>
    </div>
  );
}
