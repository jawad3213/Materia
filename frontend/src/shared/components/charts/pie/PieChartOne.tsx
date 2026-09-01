import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";

export default function PieChartOne() {
  const options: ApexOptions = {
    colors: ["#313D9C", "#465FFF", "#C6D2FD"],
    chart: {
      fontFamily: "Outfit, sans-serif",
      type: "donut",
      height: 335,
    },
    labels: ["Desktop", "Mobile", "Tablet"],
    legend: {
      show: true,
      position: "bottom",
      horizontalAlign: "center",
      fontFamily: "Outfit",
      markers: {
        shape: "circle" as any,
      },
    },
    plotOptions: {
      pie: {
        donut: {
          size: "65%",
          background: "transparent",
        },
      },
    },
    dataLabels: {
      enabled: false,
    },
    stroke: {
      width: 0,
    },
  };

  const series = [33, 50, 17];

  return (
    <div className="mx-auto flex justify-center py-8">
      <div id="chartOne" className="w-full max-w-[350px]">
        <Chart options={options} series={series} type="donut" height={335} />
      </div>
    </div>
  );
}
