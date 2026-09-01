import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";

export default function BarChartTwo() {
  const options: ApexOptions = {
    colors: ["#313D9C", "#465FFF", "#82A0FF", "#C6D2FD"],
    chart: {
      fontFamily: "Outfit, sans-serif",
      type: "bar",
      height: 335,
      stacked: true,
      toolbar: {
        show: false,
      },
      zoom: {
        enabled: false,
      },
    },
    plotOptions: {
      bar: {
        horizontal: false,
        columnWidth: "40%",
        borderRadius: 8,
        borderRadiusApplication: "end", // or "around" depending on ApexCharts version
        borderRadiusWhenStacked: "last",
      },
    },
    dataLabels: {
      enabled: false,
    },
    stroke: {
      show: false,
    },
    xaxis: {
      categories: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug"],
      axisBorder: {
        show: false,
      },
      axisTicks: {
        show: false,
      },
    },
    yaxis: {
      min: 0,
      max: 120,
      tickAmount: 6, // 0, 20, 40, 60, 80, 100, 120
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
      yaxis: {
        lines: {
          show: true,
        },
      },
      padding: {
        top: 0,
        right: 0,
        bottom: 0,
        left: 0,
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
      name: "Direct",
      data: [44, 55, 41, 67, 22, 43, 55, 41],
    },
    {
      name: "Referral",
      data: [13, 23, 20, 8, 13, 27, 13, 23],
    },
    {
      name: "Organic Search",
      data: [11, 17, 15, 15, 21, 14, 18, 20],
    },
    {
      name: "Social",
      data: [21, 7, 25, 13, 22, 8, 18, 20],
    },
  ];

  return (
    <div className="max-w-full overflow-x-auto custom-scrollbar">
      <div id="chartTwo" className="min-w-[700px]">
        <Chart options={options} series={series} type="bar" height={335} />
      </div>
    </div>
  );
}
