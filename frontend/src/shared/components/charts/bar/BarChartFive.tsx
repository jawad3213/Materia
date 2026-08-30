import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";

export default function BarChartFive() {
  const options: ApexOptions = {
    colors: ["#C6D2FD", "#465FFF"],
    chart: {
      fontFamily: "Outfit, sans-serif",
      type: "bar",
      height: 335,
      toolbar: {
        show: false,
      },
    },
    plotOptions: {
      bar: {
        horizontal: false,
        columnWidth: "40%",
        borderRadius: 3,
        borderRadiusApplication: "end",
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
      categories: [
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec",
      ],
      axisBorder: {
        show: false,
      },
      axisTicks: {
        show: false,
      },
    },
    yaxis: {
      min: 0,
      max: 100,
      tickAmount: 5,
      labels: {
        formatter: (val) => `${val}%`,
      },
    },
    legend: {
      show: false,
    },
    grid: {
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
        formatter: (val: number) => `${val}%`,
      },
    },
  };

  const series = [
    {
      name: "Series 1",
      data: [78, 60, 68, 39, 64, 44, 26, 54, 56, 49, 65, 73],
    },
    {
      name: "Series 2",
      data: [88, 49, 64, 24, 76, 67, 73, 88, 29, 68, 88, 93],
    },
  ];

  return (
    <div className="max-w-full overflow-x-auto custom-scrollbar">
      <div id="chartFive" className="min-w-[600px]">
        <Chart options={options} series={series} type="bar" height={335} />
      </div>
    </div>
  );
}
