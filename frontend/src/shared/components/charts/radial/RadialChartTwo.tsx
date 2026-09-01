import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";

export default function RadialChartTwo() {
  const options: ApexOptions = {
    chart: {
      fontFamily: "Outfit, sans-serif",
      type: "radialBar",
      height: 350,
    },
    colors: ["#313D9C", "#F2A176", "#C6D2FD"],
    plotOptions: {
      radialBar: {
        startAngle: -90,
        endAngle: 90,
        hollow: {
          size: "40%",
        },
        track: {
          background: "#F4F7FD",
          strokeWidth: "100%",
          margin: 10,
        },
        dataLabels: {
          show: false,
        },
      },
    },
    stroke: {
      lineCap: "butt",
    },
    labels: ["Series 1", "Series 2", "Series 3"],
  };

  const series = [85, 65, 45];

  return (
    <div className="mx-auto flex justify-center py-8">
      <div id="chartTwo" className="w-full max-w-[350px]">
        <Chart options={options} series={series} type="radialBar" height={350} />
      </div>
    </div>
  );
}
