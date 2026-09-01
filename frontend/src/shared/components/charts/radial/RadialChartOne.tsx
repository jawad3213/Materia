import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";

export default function RadialChartOne() {
  const options: ApexOptions = {
    chart: {
      fontFamily: "Outfit, sans-serif",
      type: "radialBar",
      height: 350,
    },
    colors: ["#465FFF"],
    plotOptions: {
      radialBar: {
        hollow: {
          size: "70%",
        },
        track: {
          background: "#F4F7FD",
          strokeWidth: "100%",
        },
        dataLabels: {
          show: true,
          name: {
            show: true,
            fontSize: "14px",
            fontWeight: 500,
            color: "#64748B",
            offsetY: -10,
          },
          value: {
            show: true,
            fontSize: "24px",
            fontWeight: 600,
            color: "#1E293B",
            offsetY: 10,
            formatter: function (val) {
              return val + "%";
            },
          },
        },
      },
    },
    stroke: {
      lineCap: "round",
    },
    labels: ["series-1"],
  };

  const series = [62.25];

  return (
    <div className="mx-auto flex justify-center py-8">
      <div id="chartOne" className="w-full max-w-[350px]">
        <Chart options={options} series={series} type="radialBar" height={350} />
      </div>
    </div>
  );
}
