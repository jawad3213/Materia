import { useRef } from "react";
import { gsap, MOTION_OK, ScrollTrigger, useGSAP } from "../animations/gsapSetup";
import { P2P_STAGES, type P2PStage } from "../content/p2pStages";
import { LANDING_ICONS } from "./landingIcons";

const SIZE = 520;
const CENTER = SIZE / 2;
const RADIUS = 196;
const LOOP_SECONDS = 13;
/** Circle drawn from the top, clockwise: the path the marker follows. */
const ORBIT_PATH = `M${CENTER},${CENTER - RADIUS} a${RADIUS},${RADIUS} 0 1,1 0,${RADIUS * 2} a${RADIUS},${RADIUS} 0 1,1 0,-${RADIUS * 2}`;

const STATUS_TONE: Record<P2PStage["tone"], string> = {
  brand: "bg-brand-50 text-brand-600 ring-brand-200 dark:bg-brand-500/15 dark:text-brand-300 dark:ring-brand-400/30",
  amber: "bg-warning-50 text-warning-700 ring-warning-200 dark:bg-warning-500/15 dark:text-warning-300 dark:ring-warning-400/30",
  sky: "bg-sky-50 text-sky-700 ring-sky-200 dark:bg-sky-500/15 dark:text-sky-300 dark:ring-sky-400/30",
  green: "bg-success-50 text-success-700 ring-success-200 dark:bg-success-500/15 dark:text-success-300 dark:ring-success-400/30",
};

/** Where a stage sits on the circle, in percent of the square. */
function nodePosition(index: number) {
  const angle = ((-90 + index * (360 / P2P_STAGES.length)) * Math.PI) / 180;
  const r = (RADIUS / SIZE) * 100;
  return { left: `${50 + r * Math.cos(angle)}%`, top: `${50 + r * Math.sin(angle)}%` };
}

/**
 * The procure-to-pay loop: a glowing marker travels around the six stages, each stage lights up as it passes and
 * the card in the middle shows the document at that stage. Clicking a stage jumps to it.
 */
export default function P2POrbit({ active, onStageChange }: { active: number; onStageChange: (index: number) => void }) {
  const root = useRef<HTMLDivElement>(null);
  const loop = useRef<gsap.core.Timeline | null>(null);
  const current = useRef(active);

  useGSAP(
    () => {
      const mm = gsap.matchMedia();
      mm.add(MOTION_OK, () => {
        const tl = gsap.timeline({
          repeat: -1,
          onUpdate: () => {
            const index = Math.min(P2P_STAGES.length - 1, Math.floor(tl.progress() * P2P_STAGES.length));
            if (index !== current.current) {
              current.current = index;
              onStageChange(index);
            }
          },
        });
        tl.to("[data-orbit-comet]", {
          motionPath: { path: "#p2p-orbit-path", align: "#p2p-orbit-path", alignOrigin: [0.5, 0.5] },
          duration: LOOP_SECONDS,
          ease: "none",
        }, 0).fromTo("[data-orbit-progress]", { strokeDashoffset: 1 }, { strokeDashoffset: 0, duration: LOOP_SECONDS, ease: "none" }, 0);
        loop.current = tl;

        gsap.to("[data-orbit-ring='a']", { rotate: 360, duration: 60, repeat: -1, ease: "none", svgOrigin: `${CENTER} ${CENTER}` });
        gsap.to("[data-orbit-ring='b']", { rotate: -360, duration: 90, repeat: -1, ease: "none", svgOrigin: `${CENTER} ${CENTER}` });
        gsap.to("[data-orbit-pulse]", { scale: 1.35, opacity: 0, duration: 1.8, repeat: -1, ease: "power1.out", transformOrigin: "50% 50%" });

        // The loop only runs while it can be seen.
        ScrollTrigger.create({ trigger: root.current, start: "top bottom", end: "bottom top", onToggle: (self) => (self.isActive ? tl.play() : tl.pause()) });

        return () => {
          loop.current = null;
        };
      });
    },
    { scope: root }
  );

  // The card in the middle changes with the stage.
  useGSAP(
    () => {
      const mm = gsap.matchMedia();
      mm.add(MOTION_OK, () => {
        gsap.fromTo("[data-orbit-card] > *", { y: 14, autoAlpha: 0 }, { y: 0, autoAlpha: 1, duration: 0.45, stagger: 0.05, ease: "power2.out" });
        gsap.fromTo(`[data-orbit-node="${active}"]`, { scale: 0.85 }, { scale: 1, duration: 0.6, ease: "elastic.out(1, 0.45)" });
      });
    },
    { scope: root, dependencies: [active] }
  );

  const jumpTo = (index: number) => {
    current.current = index;
    onStageChange(index);
    loop.current?.progress((index + 0.02) / P2P_STAGES.length);
  };

  const stage = P2P_STAGES[active];

  return (
    <div ref={root} className="relative mx-auto aspect-square w-full max-w-[520px]">
      <svg viewBox={`0 0 ${SIZE} ${SIZE}`} className="absolute inset-0 h-full w-full text-gray-900 dark:text-white" aria-hidden="true">
        <defs>
          <linearGradient id="p2p-orbit-gradient" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#465FFF" />
            <stop offset="50%" stopColor="#7A5AF8" />
            <stop offset="100%" stopColor="#0BA5EC" />
          </linearGradient>
          <filter id="p2p-glow" x="-50%" y="-50%" width="200%" height="200%">
            <feGaussianBlur stdDeviation="6" />
          </filter>
        </defs>

        <g data-orbit-ring="a">
          <circle cx={CENTER} cy={CENTER} r={150} fill="none" stroke="currentColor" strokeOpacity="0.12" strokeDasharray="2 10" />
        </g>
        <g data-orbit-ring="b">
          <circle cx={CENTER} cy={CENTER} r={238} fill="none" stroke="currentColor" strokeOpacity="0.08" strokeDasharray="1 14" />
        </g>
        <circle cx={CENTER} cy={CENTER} r={RADIUS} fill="none" stroke="currentColor" strokeOpacity="0.1" strokeWidth="1.5" />
        <path
          id="p2p-orbit-path"
          data-orbit-progress
          d={ORBIT_PATH}
          fill="none"
          stroke="url(#p2p-orbit-gradient)"
          strokeWidth="2.5"
          strokeLinecap="round"
          pathLength={1}
          strokeDasharray="1"
        />
        <g data-orbit-comet transform={`translate(${CENTER} ${CENTER - RADIUS})`}>
          <circle r="16" fill="#465FFF" opacity="0.45" filter="url(#p2p-glow)" />
          <circle r="6.5" fill="#465FFF" stroke="white" strokeWidth="2.5" />
        </g>
      </svg>

      {P2P_STAGES.map((s, index) => {
        const isActive = index === active;
        const isDone = index < active;
        return (
          <button
            key={s.key}
            type="button"
            data-orbit-node={index}
            onClick={() => jumpTo(index)}
            style={nodePosition(index)}
            className="group absolute flex -translate-x-1/2 -translate-y-1/2 flex-col items-center gap-2"
            aria-label={`${s.label} stage`}
            aria-pressed={isActive}
          >
            <span className="relative">
              {isActive && <span data-orbit-pulse aria-hidden="true" className="absolute inset-0 rounded-2xl bg-brand-400/40" />}
              <span
                className={`relative flex size-12 items-center justify-center rounded-2xl border transition-all duration-500 sm:size-14 ${
                  isActive
                    ? "border-brand-400 bg-brand-500 text-white shadow-[0_12px_32px_-8px_rgba(70,95,255,0.7)]"
                    : isDone
                      ? "border-brand-200 bg-brand-50 text-brand-500 dark:border-brand-400/40 dark:bg-brand-500/15 dark:text-brand-300"
                      : "border-gray-200 bg-white text-gray-400 shadow-theme-xs group-hover:border-brand-300 group-hover:text-brand-500 dark:border-white/10 dark:bg-gray-900 dark:text-gray-500 dark:group-hover:text-white/80"
                }`}
              >
                {LANDING_ICONS[s.icon]}
              </span>
            </span>
            <span
              className={`text-xs font-semibold tracking-wide transition-colors sm:text-sm ${
                isActive ? "text-gray-900 dark:text-white" : "text-gray-400 dark:text-gray-500"
              }`}
            >
              {s.label}
            </span>
          </button>
        );
      })}

      <div className="absolute left-1/2 top-1/2 w-[54%] -translate-x-1/2 -translate-y-1/2">
        <div
          data-orbit-card
          className="rounded-2xl border border-gray-200 bg-white/90 p-4 text-left shadow-theme-xl backdrop-blur-xl sm:p-5 dark:border-white/10 dark:bg-gray-900/90"
        >
          <div className="flex items-center justify-between gap-2">
            <span className="text-[11px] font-medium uppercase tracking-widest text-gray-400">{stage.document}</span>
            <span className={`rounded-full px-2 py-0.5 text-[11px] font-semibold ring-1 ${STATUS_TONE[stage.tone]}`}>{stage.status}</span>
          </div>
          <p className="mt-2 font-mono text-sm font-semibold text-brand-500 dark:text-brand-300">{stage.code}</p>
          <p className="mt-1 text-sm font-semibold leading-snug text-gray-900 sm:text-base dark:text-white">{stage.title}</p>
          <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">{stage.detail}</p>
          <div className="mt-3 flex items-center justify-between border-t border-gray-100 pt-3 dark:border-white/10">
            <span className="flex gap-1" aria-label={`Stage ${active + 1} of ${P2P_STAGES.length}`}>
              {P2P_STAGES.map((s, i) => (
                <span key={s.key} className={`h-1 w-4 rounded-full ${i <= active ? "bg-brand-500" : "bg-gray-200 dark:bg-white/15"}`} />
              ))}
            </span>
            {stage.amount && <span className="text-xs font-semibold text-gray-700 dark:text-white/80">{stage.amount}</span>}
          </div>
        </div>
      </div>
    </div>
  );
}
