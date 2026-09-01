import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";

export default function BarChartSix() {
  const options: ApexOptions = {
    colors: ["#465FFF", "#E5E7EB"],
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
        horizontal: true,
        barHeight: "50%",
        borderRadius: 4,
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
      categories: ["Jan", "Feb", "Mar", "Apr", "May"],
      min: 0,
      max: 700,
      tickAmount: 7, // 0, 100, 200, 300, 400, 500, 600, 700
      axisBorder: {
        show: false,
      },
      axisTicks: {
        show: false,
      },
    },
    yaxis: {
      title: {
        text: undefined,
      },
    },
    legend: {
      show: true,
      position: "top",
      horizontalAlign: "left",
      fontFamily: "Outfit",
      markers: {
        shape: "circle" as any,
      },
    },
    grid: {
      xaxis: {
        lines: {
          show: true,
        },
      },
      yaxis: {
        lines: {
          show: false,
        },
      },
    },
    fill: {
      opacity: 1,
    },
    tooltip: {
      y: {
        formatter: (val: number) => `${val}`,
      },
    },
  };

  const series = [
    {
      name: "Category A",
      data: [620, 500, 480, 610, 620],
    },
    {
      name: "Category B",
      data: [310, 505, 360, 200, 310],
    },
  ];

  return (
    <div className="max-w-full overflow-x-auto custom-scrollbar">
      <div id="chartSix" className="min-w-[500px]">
        <Chart options={options} series={series} type="bar" height={335} />
      </div>
    </div>
  );
}
