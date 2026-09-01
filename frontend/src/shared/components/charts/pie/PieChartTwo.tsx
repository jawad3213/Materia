import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";

export default function PieChartTwo() {
  const options: ApexOptions = {
    colors: ["#9d88ff", "#ff8f3c", "#ffc107", "#22c55e"],
    chart: {
      fontFamily: "Outfit, sans-serif",
      type: "donut",
      height: 335,
    },
    labels: ["Downloads", "Apps", "Documents", "Media"],
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
          size: "70%",
          background: "transparent",
          labels: {
            show: true,
            name: {
              show: true,
              fontSize: "16px",
              fontWeight: 600,
              color: "#374151",
            },
            value: {
              show: true,
              fontSize: "14px",
              fontWeight: 500,
              color: "#6B7280",
            },
            total: {
              show: true,
              showAlways: true,
              label: "Total 135 GB",
              fontSize: "16px",
              fontWeight: 600,
              color: "#374151",
              formatter: function () {
                return "160";
              },
            },
          },
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

  const series = [30, 40, 15, 15];

  return (
    <div className="mx-auto flex justify-center py-8">
      <div id="chartTwo" className="w-full max-w-[350px]">
        <Chart options={options} series={series} type="donut" height={335} />
      </div>
    </div>
  );
}
