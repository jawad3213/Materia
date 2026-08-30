import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";

export default function PieChartThree() {
  const options: ApexOptions = {
    colors: ["#82A0FF", "#80E1FF", "#C4B5FD"],
    chart: {
      fontFamily: "Outfit, sans-serif",
      type: "donut",
      height: 335,
    },
    labels: ["GPT", "Gemini", "xAI"],
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
              label: "13.5M",
              fontSize: "24px",
              fontWeight: 700,
              color: "#1F2937",
              formatter: function () {
                return "2450";
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

  const series = [35, 30, 35];

  return (
    <div className="mx-auto flex justify-center py-8">
      <div id="chartThree" className="w-full max-w-[350px]">
        <Chart options={options} series={series} type="donut" height={335} />
      </div>
    </div>
  );
}
