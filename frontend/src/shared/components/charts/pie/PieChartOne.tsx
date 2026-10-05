import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";

interface PieChartOneProps {
  series?: number[];
  labels?: string[];
  colors?: string[];
  height?: number;
  valueFormatter?: (value: number) => string;
  /** Label of the total shown in the middle of the donut; no total when omitted. */
  totalLabel?: string;
}

/** Donut chart (TailAdmin "Pie Chart 1"), data-driven, with an optional total in the centre. */
export default function PieChartOne({
  series = [33, 50, 17],
  labels = ["Desktop", "Mobile", "Tablet"],
  colors = ["#313D9C", "#465FFF", "#7592FF", "#9CB9FF", "#C6D2FD", "#E4E9FF"],
  height = 335,
  valueFormatter,
  totalLabel,
}: PieChartOneProps) {
  const format = (val: number) => (valueFormatter ? valueFormatter(val) : `${val}`);
  const options: ApexOptions = {
    colors,
    chart: { fontFamily: "Outfit, sans-serif", type: "donut", height },
    labels,
    legend: {
      show: true,
      position: "bottom",
      horizontalAlign: "center",
      fontFamily: "Outfit",
      markers: { shape: "circle" },
    },
    plotOptions: {
      pie: {
        donut: {
          size: "65%",
          background: "transparent",
          labels: totalLabel
            ? {
                show: true,
                name: { show: true, fontSize: "13px", color: "#64748B", offsetY: 18 },
                value: { show: true, fontSize: "22px", fontWeight: 600, color: "#1E293B", offsetY: -12, formatter: (val: string) => format(Number(val)) },
                total: {
                  show: true,
                  label: totalLabel,
                  fontSize: "13px",
                  color: "#64748B",
                  formatter: (w: { globals: { seriesTotals: number[] } }) => format(w.globals.seriesTotals.reduce((a, b) => a + b, 0)),
                },
              }
            : undefined,
        },
      },
    },
    dataLabels: { enabled: false },
    stroke: { width: 0 },
    tooltip: { y: { formatter: (val: number) => format(val) } },
  };

  return (
    <div className="mx-auto flex justify-center">
      <div className="w-full max-w-[380px]">
        <Chart options={options} series={series} type="donut" height={height} />
      </div>
    </div>
  );
}
