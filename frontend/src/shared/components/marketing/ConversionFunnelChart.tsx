import React from "react";
import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";

export default function ConversionFunnelChart() {
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
      name: "Ad Impressions",
      data: [44, 55, 41, 67, 22, 43, 55, 41],
    },
    {
      name: "Website Session",
      data: [13, 23, 20, 8, 13, 27, 13, 23],
    },
    {
      name: "App Download",
      data: [11, 17, 15, 15, 21, 14, 11, 20],
    },
    {
      name: "New Users",
      data: [21, 7, 25, 13, 22, 8, 25, 20],
    },
  ];

  return (
    <div className="p-6 bg-white border border-gray-200 rounded-2xl dark:bg-white/[0.03] dark:border-gray-800">
      <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90 mb-4">
        Conversion Funnel
      </h3>
      <div className="-ml-3 -mr-3">
        <Chart options={options} series={series} type="bar" height={350} />
      </div>
    </div>
  );
}
