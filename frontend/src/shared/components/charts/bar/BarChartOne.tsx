import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";

export interface BarSeries {
  name: string;
  data: number[];
}

interface BarChartOneProps {
  series?: BarSeries[];
  categories?: string[];
  colors?: string[];
  height?: number;
  /** Bars grow from the left: suited to rankings with long labels. */
  horizontal?: boolean;
  valueFormatter?: (value: number) => string;
  showLegend?: boolean;
  /** Minimum drawing width; the chart scrolls horizontally below it. */
  minWidth?: number;
}

const SAMPLE_SERIES: BarSeries[] = [{ name: "Sales", data: [168, 385, 201, 298, 187, 195, 291, 110, 215, 390, 280, 112] }];
const SAMPLE_CATEGORIES = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

/** Rounded bar chart (TailAdmin "Bar Chart 1"), data-driven. */
export default function BarChartOne({
  series = SAMPLE_SERIES,
  categories = SAMPLE_CATEGORIES,
  colors = ["#465fff"],
  height = 180,
  horizontal = false,
  valueFormatter,
  showLegend = true,
  minWidth = 1000,
}: BarChartOneProps) {
  const format = (val: number) => (valueFormatter ? valueFormatter(val) : `${val}`);
  const options: ApexOptions = {
    colors,
    chart: { fontFamily: "Outfit, sans-serif", type: "bar", height, toolbar: { show: false } },
    plotOptions: {
      bar: {
        horizontal,
        columnWidth: "39%",
        barHeight: "55%",
        borderRadius: 5,
        borderRadiusApplication: "end",
        distributed: false,
      },
    },
    dataLabels: { enabled: false },
    stroke: { show: true, width: 4, colors: ["transparent"] },
    xaxis: {
      categories,
      axisBorder: { show: false },
      axisTicks: { show: false },
      labels: horizontal ? { formatter: (val: string) => format(Number(val)) } : undefined,
    },
    legend: { show: showLegend, position: "top", horizontalAlign: "left", fontFamily: "Outfit" },
    yaxis: {
      title: { text: undefined },
      labels: horizontal ? { maxWidth: 160 } : { formatter: (val: number) => format(val) },
    },
    grid: horizontal ? { xaxis: { lines: { show: true } }, yaxis: { lines: { show: false } } } : { yaxis: { lines: { show: true } } },
    fill: { opacity: 1 },
    tooltip: { x: { show: false }, y: { formatter: (val: number) => format(val) } },
  };

  return (
    <div className="max-w-full overflow-x-auto custom-scrollbar">
      <div style={{ minWidth }}>
        <Chart options={options} series={series} type="bar" height={height} />
      </div>
    </div>
  );
}
