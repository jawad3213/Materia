import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";

export default function PieChartFour() {
  const options: ApexOptions = {
    colors: ["#C6D2FD", "#82A0FF", "#465FFF", "#313D9C"],
    chart: {
      fontFamily: "Outfit, sans-serif",
      type: "pie",
      height: 335,
    },
    labels: ["Image", "Video", "Audio", "Documents"],
    legend: {
      show: true,
      position: "bottom",
      horizontalAlign: "center",
      fontFamily: "Outfit",
      markers: {
        shape: "circle" as any,
      },
    },
    dataLabels: {
      enabled: false,
    },
    stroke: {
      width: 0,
    },
  };

  const series = [30, 20, 20, 30];

  return (
    <div className="mx-auto flex justify-center py-8">
      <div id="chartFour" className="w-full max-w-[350px]">
        <Chart options={options} series={series} type="pie" height={335} />
      </div>
    </div>
  );
}
