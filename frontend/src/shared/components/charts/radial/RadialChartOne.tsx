import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";

interface RadialChartOneProps {
  /** Percentages (0-100), one ring each. */
  series?: number[];
  labels?: string[];
  colors?: string[];
  height?: number;
}

/** Radial progress chart (TailAdmin "Radial Chart 1"), data-driven; several values draw concentric rings. */
export default function RadialChartOne({
  series = [62.25],
  labels = ["series-1"],
  colors = ["#465FFF", "#12B76A", "#F79009"],
  height = 350,
}: RadialChartOneProps) {
  const multiple = series.length > 1;
  const options: ApexOptions = {
    chart: { fontFamily: "Outfit, sans-serif", type: "radialBar", height },
    colors,
    plotOptions: {
      radialBar: {
        hollow: { size: multiple ? "45%" : "70%" },
        track: { background: "#F4F7FD", strokeWidth: "100%" },
        dataLabels: {
          show: true,
          name: { show: true, fontSize: "14px", fontWeight: 500, color: "#64748B", offsetY: -10 },
          value: { show: true, fontSize: "24px", fontWeight: 600, color: "#1E293B", offsetY: 10, formatter: (val: number) => `${val}%` },
          total: multiple ? { show: true, label: labels[0], color: "#64748B", formatter: () => `${series[0]}%` } : undefined,
        },
      },
    },
    stroke: { lineCap: "round" },
    labels,
    legend: { show: multiple, position: "bottom", fontFamily: "Outfit" },
  };

  return (
    <div className="mx-auto flex justify-center">
      <div className="w-full max-w-[350px]">
        <Chart options={options} series={series} type="radialBar" height={height} />
      </div>
    </div>
  );
}
