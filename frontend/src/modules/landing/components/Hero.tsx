import { useRef, useState } from "react";
import { Link } from "react-router-dom";
import GridShape from "../../../shared/components/common/GridShape";
import Button from "../../../shared/components/ui/button/Button";
import useAuth from "../../auth/hooks/useAuth";
import { gsap, MOTION_OK, useGSAP } from "../animations/gsapSetup";
import { P2P_STAGES } from "../content/p2pStages";
import P2POrbit from "./P2POrbit";
import { ARROW_RIGHT, CHECK } from "./landingIcons";

const TRUST = ["Separation of duties", "Three-way matching", "Never mixes currencies"];

/**
 * Opening section: the procure-to-pay loop runs on the right while the headline, a live ticker that follows the loop,
 * and the calls to action sit on the left.
 */
export default function Hero() {
  const { isAuthenticated } = useAuth();
  const root = useRef<HTMLElement>(null);
  const [stage, setStage] = useState(0);

  useGSAP(
    () => {
      const mm = gsap.matchMedia();
      mm.add(MOTION_OK, () => {
        const tl = gsap.timeline({ defaults: { ease: "power4.out" } });
        tl.fromTo("[data-hero-ticker]", { y: 16, autoAlpha: 0 }, { y: 0, autoAlpha: 1, duration: 0.6 })
          .fromTo("[data-hero-line] > span", { yPercent: 100, autoAlpha: 0 }, { yPercent: 0, autoAlpha: 1, duration: 0.9, stagger: 0.12, clearProps: "transform" }, "-=0.2")
          .fromTo("[data-hero-lead]", { y: 20, autoAlpha: 0 }, { y: 0, autoAlpha: 1, duration: 0.7, clearProps: "all" }, "-=0.6")
          .fromTo("[data-hero-cta] > *", { y: 16, autoAlpha: 0 }, { y: 0, autoAlpha: 1, duration: 0.5, stagger: 0.1, clearProps: "all" }, "-=0.5")
          .fromTo("[data-hero-trust] > *", { x: -10, autoAlpha: 0 }, { x: 0, autoAlpha: 1, duration: 0.4, stagger: 0.08, clearProps: "all" }, "-=0.3")
          .fromTo("[data-hero-panel]", { scale: 0.95, autoAlpha: 0 }, { scale: 1, autoAlpha: 1, duration: 1, ease: "expo.out", clearProps: "opacity" }, 0.2)
          .fromTo("[data-orbit-node]", { scale: 0, autoAlpha: 0 }, { scale: 1, autoAlpha: 1, duration: 0.5, stagger: 0.06, ease: "back.out(2)", clearProps: "transform,opacity,visibility" }, "-=0.8");

        // The gradient on "one continuous flow" keeps drifting.
        gsap.to("[data-hero-gradient]", { backgroundPosition: "200% 50%", duration: 6, repeat: -1, ease: "none" });
        // The loop tilts slightly with the pointer.
        const panel = root.current?.querySelector<HTMLElement>("[data-hero-panel]");
        const xTo = panel && gsap.quickTo(panel, "rotateY", { duration: 0.8, ease: "power3.out" });
        const yTo = panel && gsap.quickTo(panel, "rotateX", { duration: 0.8, ease: "power3.out" });
        const onMove = (e: PointerEvent) => {
          if (!xTo || !yTo) return;
          xTo((e.clientX / window.innerWidth - 0.5) * 8);
          yTo(-(e.clientY / window.innerHeight - 0.5) * 8);
        };
        window.addEventListener("pointermove", onMove);
        return () => window.removeEventListener("pointermove", onMove);
      });
    },
    { scope: root }
  );

  // The ticker line slides when the loop reaches a new stage.
  useGSAP(
    () => {
      const mm = gsap.matchMedia();
      mm.add(MOTION_OK, () => {
        gsap.fromTo("[data-hero-ticker-text]", { yPercent: 100, autoAlpha: 0 }, { yPercent: 0, autoAlpha: 1, duration: 0.45, ease: "power3.out" });
      });
    },
    { scope: root, dependencies: [stage] }
  );

  return (
    <section ref={root} id="overview" className="relative isolate overflow-hidden pb-20 pt-32 sm:pt-36 lg:pb-28">
      {/* Same background as the other public pages */}
      <GridShape />
      <div aria-hidden="true" className="pointer-events-none absolute inset-x-0 top-0 -z-10 h-96 bg-[radial-gradient(ellipse_at_top,#465fff26,transparent_65%)]" />

      <div className="mx-auto grid max-w-7xl items-center gap-14 px-5 sm:px-8 lg:grid-cols-[1.05fr_1fr]">
        <div className="text-center lg:text-left">
          <div
            data-hero-ticker
            className="inline-flex max-w-full items-center gap-3 rounded-full border border-gray-200 bg-white/80 py-1.5 pl-1.5 pr-4 text-sm shadow-theme-xs backdrop-blur dark:border-white/10 dark:bg-white/5"
          >
            <span className="flex items-center gap-1.5 rounded-full bg-success-500 px-2.5 py-0.5 text-xs font-semibold text-white">
              <span className="size-1.5 animate-pulse rounded-full bg-white" />
              Live
            </span>
            <span className="relative overflow-hidden">
              <span data-hero-ticker-text key={stage} className="block truncate font-medium text-gray-700 dark:text-gray-200">
                {P2P_STAGES[stage].ticker}
              </span>
            </span>
          </div>

          <h1 className="mt-7 text-[2.6rem] font-bold leading-[1.05] tracking-tight text-gray-900 sm:text-6xl xl:text-[4.25rem] dark:text-white">
            <span data-hero-line className="block overflow-hidden pb-1">
              <span className="block">Procure-to-pay,</span>
            </span>
            <span data-hero-line className="block overflow-hidden pb-2">
              <span
                data-hero-gradient
                className="block bg-[linear-gradient(90deg,#465FFF,#7A5AF8,#0BA5EC,#465FFF)] bg-[length:200%_100%] bg-clip-text text-transparent"
              >
                in one continuous flow.
              </span>
            </span>
          </h1>

          <p data-hero-lead className="mx-auto mt-6 max-w-xl text-lg leading-relaxed text-gray-500 lg:mx-0 dark:text-gray-400">
            Materia carries every purchase from the first request through approval, ordering, receiving, three-way matching and payment,
            with one controlled path and a trail back to every document.
          </p>

          <div data-hero-cta className="mt-9 flex flex-col items-center gap-3 sm:flex-row lg:justify-start sm:justify-center">
            <Link to={isAuthenticated ? "/dashboard" : "/signup"}>
              <Button endIcon={ARROW_RIGHT}>{isAuthenticated ? "Open your dashboard" : "Get started"}</Button>
            </Link>
            <button
              type="button"
              onClick={() => document.getElementById("cycle")?.scrollIntoView({ behavior: "smooth" })}
              className="group inline-flex items-center gap-2 rounded-lg px-5 py-3.5 text-sm font-medium text-gray-700 transition hover:text-brand-600 dark:text-gray-300"
            >
              <span className="flex size-8 items-center justify-center rounded-full border border-gray-300 transition group-hover:border-brand-400 dark:border-white/20">
                <svg className="size-3.5 translate-x-px" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                  <path d="M8 5v14l11-7z" />
                </svg>
              </span>
              See how it works
            </button>
          </div>

          <ul data-hero-trust className="mt-10 flex flex-wrap items-center justify-center gap-x-6 gap-y-3 lg:justify-start">
            {TRUST.map((item) => (
              <li key={item} className="flex items-center gap-2 text-sm font-medium text-gray-600 dark:text-gray-400">
                <span className="flex size-5 items-center justify-center rounded-full bg-success-50 text-success-600 dark:bg-success-500/15 dark:text-success-400">
                  {CHECK}
                </span>
                {item}
              </li>
            ))}
          </ul>
        </div>

        <div className="[perspective:1400px]">
          <div data-hero-panel className="relative [transform-style:preserve-3d]">
            <div className="mb-2 flex items-center justify-between">
              <span className="text-xs font-semibold uppercase tracking-[0.2em] text-gray-400">Procure-to-pay loop</span>
              <span className="rounded-full border border-gray-200 bg-white px-2.5 py-1 text-[11px] text-gray-500 dark:border-white/10 dark:bg-white/5 dark:text-gray-400">
                Example purchase
              </span>
            </div>
            <P2POrbit active={stage} onStageChange={setStage} />
          </div>
        </div>
      </div>
    </section>
  );
}
