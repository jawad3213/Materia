import { useRef } from "react";
import { Link } from "react-router-dom";
import Button from "../../../shared/components/ui/button/Button";
import GridShape from "../../../shared/components/common/GridShape";
import useAuth from "../../auth/hooks/useAuth";
import { gsap, MOTION_OK, SplitText, useGSAP } from "../animations/gsapSetup";
import DashboardPreview from "./DashboardPreview";
import { ARROW_RIGHT, LANDING_ICONS } from "./landingIcons";

const CHIPS = [
  { icon: LANDING_ICONS.approval, text: "Approved by Sara", sub: "REQ-2026-0142", className: "-left-4 top-24 sm:-left-10" },
  { icon: LANDING_ICONS.receipt, text: "48 of 50 accepted", sub: "GR-2026-0087", className: "-right-3 top-1/2 sm:-right-8" },
  { icon: LANDING_ICONS.invoice, text: "Three-way match ✓", sub: "INV-2026-0311", className: "bottom-10 left-6 sm:-left-6" },
];

/** Opening section: animated headline, calls to action and a living dashboard preview. */
export default function Hero() {
  const { isAuthenticated } = useAuth();
  const root = useRef<HTMLElement>(null);

  useGSAP(
    () => {
      const mm = gsap.matchMedia();
      mm.add(MOTION_OK, () => {
        const split = SplitText.create("[data-hero-title]", { type: "words,lines", mask: "lines" });
        const tl = gsap.timeline({ defaults: { ease: "power4.out" } });
        tl.from("[data-hero-badge]", { y: 20, autoAlpha: 0, duration: 0.6 })
          .from(split.words, { yPercent: 110, duration: 1, stagger: 0.05 }, "-=0.2")
          .from("[data-hero-lead]", { y: 24, autoAlpha: 0, duration: 0.8 }, "-=0.6")
          .from("[data-hero-cta] > *", { y: 20, autoAlpha: 0, duration: 0.6, stagger: 0.1 }, "-=0.5")
          .from("[data-hero-preview]", { y: 80, rotateX: 18, autoAlpha: 0, duration: 1.4, transformPerspective: 1200 }, "-=0.6")
          .from("[data-preview-kpi]", { y: 16, autoAlpha: 0, duration: 0.5, stagger: 0.08 }, "-=0.8")
          .from("[data-preview-chart]", { scale: 0.96, autoAlpha: 0, duration: 0.6, stagger: 0.12 }, "-=0.5")
          .from("[data-hero-chip]", { scale: 0.6, autoAlpha: 0, duration: 0.6, stagger: 0.15, ease: "back.out(2)" }, "-=0.3");

        // Chips float gently once they have landed.
        gsap.utils.toArray<HTMLElement>("[data-hero-chip]").forEach((chip, i) => {
          gsap.to(chip, { y: i % 2 ? 10 : -10, duration: 2.6 + i * 0.4, repeat: -1, yoyo: true, ease: "sine.inOut", delay: 2.2 });
        });
        // The glow behind the preview drifts slowly.
        gsap.to("[data-hero-glow]", { rotate: 360, duration: 40, repeat: -1, ease: "none" });
        // The preview settles back as the visitor scrolls away.
        gsap.to("[data-hero-preview]", {
          yPercent: -8,
          scale: 0.97,
          ease: "none",
          scrollTrigger: { trigger: root.current, start: "top top", end: "bottom top", scrub: true },
        });
        return () => split.revert();
      });
    },
    { scope: root }
  );

  return (
    <section ref={root} id="overview" className="relative isolate overflow-hidden pb-24 pt-32 sm:pt-40">
      <GridShape />
      <div
        data-hero-glow
        aria-hidden="true"
        className="pointer-events-none absolute left-1/2 top-[38%] -z-10 h-[620px] w-[620px] -translate-x-1/2 rounded-full bg-[conic-gradient(from_90deg,#465fff33,#12b76a26,#f7900926,#465fff33)] blur-3xl"
      />

      <div className="mx-auto max-w-7xl px-5 sm:px-8">
        <div className="mx-auto max-w-4xl text-center">
          <span
            data-hero-badge
            className="inline-flex items-center gap-2 rounded-full border border-brand-200 bg-brand-50 px-4 py-1.5 text-sm font-medium text-brand-600 dark:border-brand-500/30 dark:bg-brand-500/10 dark:text-brand-300"
          >
            <span className="size-2 animate-pulse rounded-full bg-brand-500" />
            Procurement, from request to payment
          </span>
          <h1 data-hero-title className="mt-6 text-4xl font-bold leading-[1.1] tracking-tight text-gray-900 sm:text-6xl lg:text-title-xl dark:text-white">
            Every purchase, controlled from the first request to the last payment.
          </h1>
          <p data-hero-lead className="mx-auto mt-6 max-w-2xl text-lg leading-relaxed text-gray-500 sm:text-xl dark:text-gray-400">
            Materia connects requisitions, purchase orders, receiving, returns, invoices and payments in one system, with approvals,
            three-way matching and stock that stay in step.
          </p>
          <div data-hero-cta className="mt-10 flex flex-col items-center justify-center gap-3 sm:flex-row">
            <Link to={isAuthenticated ? "/dashboard" : "/login"}>
              <Button endIcon={ARROW_RIGHT}>{isAuthenticated ? "Open your dashboard" : "Sign in to Materia"}</Button>
            </Link>
            <Link to="/features">
              <Button variant="outline">Explore the features</Button>
            </Link>
          </div>
        </div>

        <div className="relative mx-auto mt-20 max-w-5xl [perspective:1200px]">
          <div data-hero-preview>
            <DashboardPreview />
          </div>
          {CHIPS.map((chip) => (
            <div
              key={chip.sub}
              data-hero-chip
              className={`absolute z-10 hidden items-center gap-3 rounded-xl border border-gray-200 bg-white/95 px-4 py-3 shadow-theme-lg backdrop-blur sm:flex dark:border-white/10 dark:bg-gray-900/95 ${chip.className}`}
            >
              <span className="flex size-9 items-center justify-center rounded-lg bg-brand-50 text-brand-500 dark:bg-brand-500/15">{chip.icon}</span>
              <span>
                <span className="block text-sm font-semibold text-gray-800 dark:text-white/90">{chip.text}</span>
                <span className="block font-mono text-[11px] text-gray-400">{chip.sub}</span>
              </span>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
