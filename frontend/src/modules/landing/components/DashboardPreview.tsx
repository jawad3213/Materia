import { useRef } from "react";
import Badge from "../../../shared/components/ui/badge/Badge";
import { gsap, MOTION_OK, useGSAP } from "../animations/gsapSetup";
import type { IconName } from "../content/landingContent";
import { LANDING_ICONS } from "./landingIcons";

/*
 * Illustrative figures for the preview only; the real dashboard computes them from live documents.
 * Everything is drawn in SVG so it renders crisply at any size and every piece can be animated.
 */

type Point = [number, number];

/** Smooth curve through the points (Catmull-Rom converted to cubic Béziers). */
function smoothPath(points: Point[]) {
  let d = `M${points[0][0]},${points[0][1]}`;
  for (let i = 0; i < points.length - 1; i++) {
    const p0 = points[i - 1] ?? points[i];
    const p1 = points[i];
    const p2 = points[i + 1];
    const p3 = points[i + 2] ?? p2;
    const c1: Point = [p1[0] + (p2[0] - p0[0]) / 6, p1[1] + (p2[1] - p0[1]) / 6];
    const c2: Point = [p2[0] - (p3[0] - p1[0]) / 6, p2[1] - (p3[1] - p1[1]) / 6];
    d += ` C${c1[0].toFixed(1)},${c1[1].toFixed(1)} ${c2[0].toFixed(1)},${c2[1].toFixed(1)} ${p2[0]},${p2[1]}`;
  }
  return d;
}

// ---- KPI cards -------------------------------------------------------------------------------------------------

interface Kpi {
  label: string;
  value: number;
  decimals: number;
  prefix?: string;
  suffix: string;
  delta: string;
  deltaTone: "up" | "warn" | "neutral";
  spark: number[];
  color: string;
}

const KPIS: Kpi[] = [
  { label: "Spend this quarter", value: 1.24, decimals: 2, suffix: "M MAD", delta: "+12.4%", deltaTone: "up", spark: [8, 12, 10, 15, 14, 19, 18, 24], color: "#465FFF" },
  { label: "Open purchase orders", value: 24, decimals: 0, suffix: "", delta: "3 late", deltaTone: "warn", spark: [18, 21, 19, 26, 23, 22, 25, 24], color: "#0BA5EC" },
  { label: "Invoices to pay", value: 18.4, decimals: 1, suffix: "K MAD", delta: "6 due this week", deltaTone: "neutral", spark: [22, 18, 20, 15, 17, 13, 16, 12], color: "#F79009" },
  { label: "Accepted at receipt", value: 97.2, decimals: 1, suffix: "%", delta: "+0.8 pt", deltaTone: "up", spark: [94, 95, 94.5, 96, 95.8, 96.6, 96.9, 97.2], color: "#12B76A" },
];

const DELTA_TONE: Record<Kpi["deltaTone"], string> = {
  up: "bg-success-50 text-success-600 dark:bg-success-500/15 dark:text-success-400",
  warn: "bg-warning-50 text-warning-600 dark:bg-warning-500/15 dark:text-warning-400",
  neutral: "bg-gray-100 text-gray-600 dark:bg-white/5 dark:text-gray-400",
};

function sparkline(values: number[]) {
  const min = Math.min(...values);
  const max = Math.max(...values);
  const pts = values.map((v, i): Point => [Math.round((i / (values.length - 1)) * 100), Math.round(28 - ((v - min) / (max - min || 1)) * 24)]);
  const line = smoothPath(pts);
  return { line, area: `${line} L100,32 L0,32 Z` };
}

// ---- Ordered vs paid area chart --------------------------------------------------------------------------------

const MONTHS = ["May", "Jun", "Jul", "Aug", "Sep", "Oct"];
const ORDERED = [42, 55, 48, 66, 72, 81];
const PAID = [30, 41, 45, 52, 60, 69];
const CW = 560;
const CH = 210;
const PAD = { left: 34, right: 14, top: 18, bottom: 28 };
const Y_MAX = 100;
const HIGHLIGHT = 4;

const xAt = (i: number) => PAD.left + (i * (CW - PAD.left - PAD.right)) / (MONTHS.length - 1);
const yAt = (v: number) => PAD.top + (1 - v / Y_MAX) * (CH - PAD.top - PAD.bottom);
const toPoints = (values: number[]) => values.map((v, i): Point => [Math.round(xAt(i)), Math.round(yAt(v))]);
const areaUnder = (line: string) => `${line} L${xAt(MONTHS.length - 1)},${CH - PAD.bottom} L${PAD.left},${CH - PAD.bottom} Z`;

const ORDERED_LINE = smoothPath(toPoints(ORDERED));
const PAID_LINE = smoothPath(toPoints(PAID));

// ---- Orders by status donut ------------------------------------------------------------------------------------

const STATUSES = [
  { label: "Confirmed", value: 9, color: "#465FFF" },
  { label: "To receive", value: 6, color: "#0BA5EC" },
  { label: "Completed", value: 5, color: "#12B76A" },
  { label: "Draft", value: 4, color: "#98A2B3" },
];
const STATUS_TOTAL = STATUSES.reduce((sum, s) => sum + s.value, 0);
const SEGMENTS = STATUSES.reduce<{ label: string; color: string; length: number; start: number }[]>((acc, s) => {
  const start = acc.length ? acc[acc.length - 1].start + acc[acc.length - 1].length + 1.5 : 0;
  return [...acc, { label: s.label, color: s.color, length: (s.value / STATUS_TOTAL) * 100 - 1.5, start }];
}, []);

// ---- Spend by category, three-way match, activity ---------------------------------------------------------------

const CATEGORIES = [
  { label: "Raw materials", value: 412 },
  { label: "MRO & spares", value: 268 },
  { label: "Packaging", value: 191 },
  { label: "Electronics", value: 154 },
  { label: "Services", value: 97 },
];
const CATEGORY_MAX = CATEGORIES[0].value;

const MATCH_PERCENT = 94;
const MATCH_BREAKDOWN = [
  { label: "Matched", value: "94%", color: "bg-success-500" },
  { label: "Quantity variance", value: "4%", color: "bg-warning-500" },
  { label: "Price variance", value: "2%", color: "bg-error-500" },
];

const ACTIVITY = [
  { code: "INV-2026-0311", text: "matched to order and receipt", time: "2 min", dot: "bg-success-500" },
  { code: "GR-2026-0087", text: "posted, 2 units rejected", time: "18 min", dot: "bg-warning-500" },
  { code: "PO-2026-0214", text: "confirmed by the supplier", time: "1 h", dot: "bg-brand-500" },
  { code: "REQ-2026-0142", text: "approved by a manager", time: "3 h", dot: "bg-success-500" },
  { code: "PAY-2026-0098", text: "executed by bank transfer", time: "5 h", dot: "bg-sky-500" },
];

const NAV: IconName[] = ["dashboard", "requisition", "order", "receipt", "invoice", "payment", "supplier", "stock"];

const card = "rounded-xl border border-gray-100 bg-white p-4 dark:border-white/5 dark:bg-white/[0.02]";
const cardTitle = "text-sm font-semibold text-gray-800 dark:text-white/90";

/** A stylised Materia dashboard window: KPIs, trends, status mix, category spend, matching quality and live activity. */
export default function DashboardPreview() {
  const root = useRef<HTMLDivElement>(null);

  useGSAP(
    () => {
      const mm = gsap.matchMedia();
      mm.add(MOTION_OK, () => {
        const trigger = { trigger: root.current, start: "top 75%", once: true };
        const tl = gsap.timeline({ defaults: { ease: "power3.out" }, scrollTrigger: trigger });
        tl.from("[data-dp-kpi]", { y: 20, autoAlpha: 0, duration: 0.6, stagger: 0.08 })
          .from("[data-dp-draw]", { attr: { "stroke-dashoffset": 1 }, duration: 1.6, ease: "power2.inOut", stagger: 0.05 }, "-=0.3")
          .from("[data-dp-fill]", { autoAlpha: 0, duration: 1.2 }, "-=1.1")
          .from("[data-dp-seg]", { attr: { "stroke-dasharray": "0 100" }, duration: 1, stagger: 0.12, ease: "power2.out" }, "-=1.4")
          .from("[data-dp-bar]", { scaleX: 0, transformOrigin: "left center", duration: 1, stagger: 0.08 }, "-=1")
          .from("[data-dp-gauge]", { attr: { "stroke-dasharray": "0 1" }, duration: 1.4, ease: "power2.inOut" }, "-=1.1")
          .from("[data-dp-activity] > li", { x: 18, autoAlpha: 0, duration: 0.5, stagger: 0.08 }, "-=1")
          .from("[data-dp-tooltip]", { scale: 0.85, autoAlpha: 0, duration: 0.5, ease: "back.out(2)" }, "-=0.4");

        // Figures count up as the window comes into view.
        gsap.utils.toArray<HTMLElement>("[data-dp-count]").forEach((el) => {
          const end = Number(el.dataset.dpCount);
          const decimals = Number(el.dataset.decimals ?? 0);
          const counter = { value: 0 };
          el.textContent = (0).toFixed(decimals);
          gsap.to(counter, {
            value: end,
            duration: 1.8,
            ease: "power2.out",
            scrollTrigger: trigger,
            onUpdate: () => {
              el.textContent = counter.value.toFixed(decimals);
            },
          });
        });

        gsap.to("[data-dp-pulse]", { attr: { r: 14 }, opacity: 0, duration: 1.6, repeat: -1, ease: "power1.out" });
      });
    },
    { scope: root }
  );

  const tipX = (xAt(HIGHLIGHT) / CW) * 100;

  return (
    <div ref={root} className="relative overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-theme-xl dark:border-white/10 dark:bg-gray-900">
      {/* Window bar */}
      <div className="flex items-center gap-2 border-b border-gray-100 px-4 py-3 dark:border-white/5">
        <span className="size-3 rounded-full bg-error-400" />
        <span className="size-3 rounded-full bg-warning-400" />
        <span className="size-3 rounded-full bg-success-400" />
        <span className="mx-auto hidden w-full max-w-xs items-center justify-center gap-1.5 rounded-md bg-gray-50 px-3 py-1 text-[11px] text-gray-400 sm:flex dark:bg-white/5">
          <svg className="size-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2} aria-hidden="true">
            <rect x="5" y="11" width="14" height="10" rx="2" />
            <path d="M8 11V7a4 4 0 0 1 8 0v4" />
          </svg>
          app.materia / dashboard
        </span>
        <span className="ml-auto shrink-0 sm:ml-0">
          <Badge size="sm" color="light">
            Illustrative data
          </Badge>
        </span>
      </div>

      <div className="flex">
        {/* App sidebar */}
        <aside className="hidden w-14 shrink-0 flex-col items-center gap-2 border-r border-gray-100 py-4 sm:flex dark:border-white/5">
          {NAV.map((icon, i) => (
            <span
              key={icon}
              className={`flex size-9 items-center justify-center rounded-lg [&>svg]:size-[18px] ${
                i === 0 ? "bg-brand-50 text-brand-500 dark:bg-brand-500/15 dark:text-brand-300" : "text-gray-400"
              }`}
            >
              {LANDING_ICONS[icon]}
            </span>
          ))}
        </aside>

        <div className="min-w-0 flex-1 space-y-4 bg-gray-50/60 p-4 sm:p-5 dark:bg-transparent">
          {/* Page header */}
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <p className="text-base font-semibold text-gray-900 dark:text-white">Procurement overview</p>
              <p className="flex items-center gap-1.5 text-xs text-gray-500 dark:text-gray-400">
                <span className="relative flex size-2">
                  <span className="absolute inline-flex size-full animate-ping rounded-full bg-success-400 opacity-75" />
                  <span className="relative inline-flex size-2 rounded-full bg-success-500" />
                </span>
                Live · Q4 2026 · all sites
              </p>
            </div>
            <div className="flex rounded-lg bg-gray-100 p-0.5 text-xs font-medium dark:bg-white/5">
              {["30D", "90D", "12M"].map((period) => (
                <span
                  key={period}
                  className={`rounded-md px-3 py-1.5 ${period === "90D" ? "bg-white text-gray-900 shadow-theme-xs dark:bg-gray-800 dark:text-white" : "text-gray-500"}`}
                >
                  {period}
                </span>
              ))}
            </div>
          </div>

          {/* KPIs */}
          <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
            {KPIS.map((k, index) => {
              const spark = sparkline(k.spark);
              const id = `dp-spark-${index}`;
              return (
                <div key={k.label} data-dp-kpi className={`${card} relative overflow-hidden`}>
                  <p className="truncate text-xs text-gray-500 dark:text-gray-400">{k.label}</p>
                  <p className="mt-1.5 flex items-baseline gap-1 text-xl font-bold tracking-tight text-gray-900 sm:text-2xl dark:text-white">
                    <span data-dp-count={k.value} data-decimals={k.decimals}>
                      {k.value.toFixed(k.decimals)}
                    </span>
                    {k.suffix && <span className="text-xs font-semibold text-gray-400">{k.suffix}</span>}
                  </p>
                  <span className={`mt-2 inline-flex rounded-full px-2 py-0.5 text-[11px] font-semibold ${DELTA_TONE[k.deltaTone]}`}>{k.delta}</span>
                  <svg viewBox="0 0 100 32" preserveAspectRatio="none" className="absolute bottom-3 right-3 h-9 w-20 sm:w-24" aria-hidden="true">
                    <defs>
                      <linearGradient id={id} x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stopColor={k.color} stopOpacity="0.25" />
                        <stop offset="100%" stopColor={k.color} stopOpacity="0" />
                      </linearGradient>
                    </defs>
                    <path data-dp-fill d={spark.area} fill={`url(#${id})`} />
                    <path
                      data-dp-draw
                      d={spark.line}
                      fill="none"
                      stroke={k.color}
                      strokeWidth="2"
                      strokeLinecap="round"
                      vectorEffect="non-scaling-stroke"
                      pathLength={1}
                      strokeDasharray="1"
                      strokeDashoffset="0"
                    />
                  </svg>
                </div>
              );
            })}
          </div>

          {/* Trend + status */}
          <div className="grid gap-3 lg:grid-cols-5">
            <div className={`${card} lg:col-span-3`}>
              <div className="flex flex-wrap items-center justify-between gap-2">
                <p className={cardTitle}>Ordered vs paid</p>
                <div className="flex items-center gap-4 text-xs text-gray-500 dark:text-gray-400">
                  <span className="flex items-center gap-1.5">
                    <span className="h-1.5 w-4 rounded-full bg-brand-500" />
                    Ordered
                  </span>
                  <span className="flex items-center gap-1.5">
                    <span className="h-1.5 w-4 rounded-full bg-success-500" />
                    Paid
                  </span>
                </div>
              </div>
              <div className="relative mt-3">
                <svg viewBox={`0 0 ${CW} ${CH}`} className="h-auto w-full overflow-visible" aria-hidden="true">
                  <defs>
                    <linearGradient id="dp-ordered" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#465FFF" stopOpacity="0.28" />
                      <stop offset="100%" stopColor="#465FFF" stopOpacity="0" />
                    </linearGradient>
                    <linearGradient id="dp-paid" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#12B76A" stopOpacity="0.22" />
                      <stop offset="100%" stopColor="#12B76A" stopOpacity="0" />
                    </linearGradient>
                  </defs>
                  {[0, 25, 50, 75, 100].map((tick) => (
                    <g key={tick}>
                      <line
                        x1={PAD.left}
                        x2={CW - PAD.right}
                        y1={yAt(tick)}
                        y2={yAt(tick)}
                        className="stroke-gray-100 dark:stroke-white/5"
                        strokeDasharray={tick === 0 ? undefined : "3 4"}
                      />
                      <text x={PAD.left - 8} y={yAt(tick) + 4} textAnchor="end" className="fill-gray-400 text-[11px]">
                        {tick === 0 ? "0" : `${tick}K`}
                      </text>
                    </g>
                  ))}
                  {MONTHS.map((m, i) => (
                    <text key={m} x={xAt(i)} y={CH - 6} textAnchor="middle" className={`text-[11px] ${i === HIGHLIGHT ? "fill-gray-700 font-semibold dark:fill-white" : "fill-gray-400"}`}>
                      {m}
                    </text>
                  ))}
                  <path data-dp-fill d={areaUnder(ORDERED_LINE)} fill="url(#dp-ordered)" />
                  <path data-dp-fill d={areaUnder(PAID_LINE)} fill="url(#dp-paid)" />
                  <path data-dp-draw d={ORDERED_LINE} fill="none" stroke="#465FFF" strokeWidth="2.5" strokeLinecap="round" pathLength={1} strokeDasharray="1" strokeDashoffset="0" />
                  <path data-dp-draw d={PAID_LINE} fill="none" stroke="#12B76A" strokeWidth="2.5" strokeLinecap="round" pathLength={1} strokeDasharray="1" strokeDashoffset="0" />
                  <line x1={xAt(HIGHLIGHT)} x2={xAt(HIGHLIGHT)} y1={PAD.top} y2={CH - PAD.bottom} className="stroke-gray-300 dark:stroke-white/20" strokeDasharray="4 4" />
                  {[
                    { v: ORDERED[HIGHLIGHT], color: "#465FFF" },
                    { v: PAID[HIGHLIGHT], color: "#12B76A" },
                  ].map((p) => (
                    <g key={p.color}>
                      <circle data-dp-pulse cx={xAt(HIGHLIGHT)} cy={yAt(p.v)} r="6" fill={p.color} opacity="0.35" />
                      <circle cx={xAt(HIGHLIGHT)} cy={yAt(p.v)} r="5" fill={p.color} stroke="white" strokeWidth="2.5" />
                    </g>
                  ))}
                </svg>
                <div
                  data-dp-tooltip
                  className="pointer-events-none absolute top-0 min-w-[132px] -translate-x-[calc(100%+12px)] rounded-lg border border-gray-200 bg-white px-3 py-2 text-xs shadow-theme-lg dark:border-white/10 dark:bg-gray-800"
                  style={{ left: `${tipX}%` }}
                >
                  <p className="font-semibold text-gray-800 dark:text-white">September 2026</p>
                  <p className="mt-1 flex items-center justify-between gap-3 text-gray-500 dark:text-gray-400">
                    <span className="flex items-center gap-1.5">
                      <span className="size-2 rounded-full bg-brand-500" />
                      Ordered
                    </span>
                    <span className="font-semibold text-gray-800 dark:text-white">{ORDERED[HIGHLIGHT]}K</span>
                  </p>
                  <p className="mt-0.5 flex items-center justify-between gap-3 text-gray-500 dark:text-gray-400">
                    <span className="flex items-center gap-1.5">
                      <span className="size-2 rounded-full bg-success-500" />
                      Paid
                    </span>
                    <span className="font-semibold text-gray-800 dark:text-white">{PAID[HIGHLIGHT]}K</span>
                  </p>
                </div>
              </div>
            </div>

            <div className={`${card} lg:col-span-2`}>
              <p className={cardTitle}>Orders by status</p>
              <div className="mt-3 flex items-center gap-5">
                <div className="relative size-32 shrink-0 sm:size-36">
                  <svg viewBox="0 0 120 120" className="size-full -rotate-90" aria-hidden="true">
                    <circle cx="60" cy="60" r="46" fill="none" strokeWidth="14" className="stroke-gray-100 dark:stroke-white/5" />
                    {SEGMENTS.map((s) => (
                      <circle
                        key={s.label}
                        data-dp-seg
                        cx="60"
                        cy="60"
                        r="46"
                        fill="none"
                        stroke={s.color}
                        strokeWidth="14"
                        pathLength={100}
                        strokeDasharray={`${s.length} ${100 - s.length}`}
                        strokeDashoffset={-s.start}
                      />
                    ))}
                  </svg>
                  <div className="absolute inset-0 flex flex-col items-center justify-center">
                    <span data-dp-count={STATUS_TOTAL} className="text-2xl font-bold text-gray-900 dark:text-white">
                      {STATUS_TOTAL}
                    </span>
                    <span className="text-[11px] text-gray-400">open orders</span>
                  </div>
                </div>
                <ul className="min-w-0 flex-1 space-y-2.5">
                  {STATUSES.map((s) => (
                    <li key={s.label} className="flex items-center justify-between gap-2 text-xs">
                      <span className="flex min-w-0 items-center gap-2 text-gray-600 dark:text-gray-300">
                        <span className="size-2.5 shrink-0 rounded-sm" style={{ background: s.color }} />
                        <span className="truncate">{s.label}</span>
                      </span>
                      <span className="font-semibold text-gray-800 dark:text-white">
                        {s.value}
                        <span className="ml-1 font-normal text-gray-400">{Math.round((s.value / STATUS_TOTAL) * 100)}%</span>
                      </span>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          </div>

          {/* Category spend, matching, activity */}
          <div className="grid gap-3 md:grid-cols-2 lg:grid-cols-3">
            <div className={card}>
              <div className="flex items-center justify-between">
                <p className={cardTitle}>Spend by category</p>
                <span className="text-[11px] text-gray-400">K MAD</span>
              </div>
              <ul className="mt-4 space-y-3">
                {CATEGORIES.map((c, i) => (
                  <li key={c.label}>
                    <div className="flex justify-between text-xs">
                      <span className="text-gray-600 dark:text-gray-300">{c.label}</span>
                      <span className="font-semibold text-gray-800 dark:text-white">{c.value}</span>
                    </div>
                    <div className="mt-1.5 h-2 overflow-hidden rounded-full bg-gray-100 dark:bg-white/5">
                      <div
                        data-dp-bar
                        className="h-full rounded-full bg-gradient-to-r from-brand-500 to-brand-300"
                        style={{ width: `${(c.value / CATEGORY_MAX) * 100}%`, opacity: 1 - i * 0.12 }}
                      />
                    </div>
                  </li>
                ))}
              </ul>
            </div>

            <div className={card}>
              <p className={cardTitle}>Three-way match</p>
              <div className="relative mx-auto mt-3 w-full max-w-[220px]">
                <svg viewBox="0 0 160 92" className="w-full" aria-hidden="true">
                  <defs>
                    <linearGradient id="dp-gauge" x1="0" y1="0" x2="1" y2="0">
                      <stop offset="0%" stopColor="#12B76A" />
                      <stop offset="100%" stopColor="#32D583" />
                    </linearGradient>
                  </defs>
                  <path d="M16 82 A64 64 0 0 1 144 82" fill="none" strokeWidth="12" strokeLinecap="round" className="stroke-gray-100 dark:stroke-white/5" />
                  <path
                    data-dp-gauge
                    d="M16 82 A64 64 0 0 1 144 82"
                    fill="none"
                    stroke="url(#dp-gauge)"
                    strokeWidth="12"
                    strokeLinecap="round"
                    pathLength={1}
                    strokeDasharray={`${MATCH_PERCENT / 100} 1`}
                  />
                </svg>
                <div className="absolute inset-x-0 bottom-0 text-center">
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">
                    <span data-dp-count={MATCH_PERCENT}>{MATCH_PERCENT}</span>%
                  </p>
                  <p className="text-[11px] text-gray-400">first-pass match rate</p>
                </div>
              </div>
              <ul className="mt-4 space-y-2">
                {MATCH_BREAKDOWN.map((m) => (
                  <li key={m.label} className="flex items-center justify-between text-xs">
                    <span className="flex items-center gap-2 text-gray-600 dark:text-gray-300">
                      <span className={`size-2 rounded-full ${m.color}`} />
                      {m.label}
                    </span>
                    <span className="font-semibold text-gray-800 dark:text-white">{m.value}</span>
                  </li>
                ))}
              </ul>
            </div>

            <div className={`${card} md:col-span-2 lg:col-span-1`}>
              <div className="flex items-center justify-between">
                <p className={cardTitle}>Recent activity</p>
                <span className="text-[11px] font-medium text-brand-500">View all</span>
              </div>
              <ul data-dp-activity className="relative mt-4 space-y-3.5 before:absolute before:bottom-1 before:left-[4px] before:top-1 before:w-px before:bg-gray-100 dark:before:bg-white/5">
                {ACTIVITY.map((a) => (
                  <li key={a.code} className="relative flex gap-3 pl-0">
                    <span className={`relative mt-1 size-[9px] shrink-0 rounded-full ring-4 ring-white dark:ring-gray-900 ${a.dot}`} />
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-xs text-gray-600 dark:text-gray-300">
                        <span className="font-mono font-semibold text-gray-800 dark:text-white">{a.code}</span> {a.text}
                      </p>
                      <p className="text-[11px] text-gray-400">{a.time} ago</p>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
