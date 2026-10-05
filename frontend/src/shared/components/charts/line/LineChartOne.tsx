import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";

export interface LineSeries {
  name: string;
  data: number[];
}

interface LineChartOneProps {
  /** One area per series; defaults to the template's sample data. */
  series?: LineSeries[];
  categories?: string[];
  colors?: string[];
  height?: number;
  /** Formats values in the tooltip and on the y axis. */
  valueFormatter?: (value: number) => string;
  showLegend?: boolean;
  /** Minimum drawing width; the chart scrolls horizontally below it. */
  minWidth?: number;
}

const SAMPLE_SERIES: LineSeries[] = [
  { name: "Sales", data: [180, 190, 170, 160, 175, 165, 170, 205, 230, 210, 240, 235] },
  { name: "Revenue", data: [40, 30, 50, 40, 55, 40, 70, 100, 110, 120, 150, 140] },
];
const SAMPLE_CATEGORIES = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

/** Gradient area chart (TailAdmin "Line Chart 1"), data-driven. */
export default function LineChartOne({
  series = SAMPLE_SERIES,
  categories = SAMPLE_CATEGORIES,
  colors = ["#465FFF", "#9CB9FF", "#12B76A"],
  height = 310,
  valueFormatter,
  showLegend = false,
  minWidth = 1000,
}: LineChartOneProps) {
  const options: ApexOptions = {
    legend: { show: showLegend, position: "top", horizontalAlign: "left", fontFamily: "Outfit" },
    colors,
    chart: { fontFamily: "Outfit, sans-serif", height, type: "line", toolbar: { show: false } },
    stroke: { curve: "smooth", width: series.map(() => 2) },
    fill: { type: "gradient", gradient: { opacityFrom: 0.55, opacityTo: 0 } },
    markers: { size: 0, strokeColors: "#fff", strokeWidth: 2, hover: { size: 6 } },
    grid: { xaxis: { lines: { show: false } }, yaxis: { lines: { show: true } } },
    dataLabels: { enabled: false },
    tooltip: { enabled: true, y: valueFormatter ? { formatter: (val: number) => valueFormatter(val) } : undefined },
    xaxis: {
      type: "category",
      categories,
      axisBorder: { show: false },
      axisTicks: { show: false },
      tooltip: { enabled: false },
    },
    yaxis: {
      labels: {
        style: { fontSize: "12px", colors: ["#6B7280"] },
        formatter: valueFormatter ? (val: number) => valueFormatter(val) : undefined,
      },
      title: { text: "", style: { fontSize: "0px" } },
    },
  };

  return (
    <div className="max-w-full overflow-x-auto custom-scrollbar">
      <div style={{ minWidth }}>
        <Chart options={options} series={series} type="area" height={height} />
      </div>
    </div>
  );
}
