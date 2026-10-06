import { useRef } from "react";
import { gsap, MOTION_OK, useGSAP } from "../animations/gsapSetup";
import DashboardPreview from "./DashboardPreview";
import SectionHeading from "./SectionHeading";
import { LANDING_ICONS } from "./landingIcons";

const CHIPS = [
  { icon: LANDING_ICONS.approval, text: "Approved by Sara", sub: "REQ-2026-0142", className: "-left-56 top-24" },
  { icon: LANDING_ICONS.receipt, text: "48 of 50 accepted", sub: "GR-2026-0087", className: "-right-56 top-1/2" },
  { icon: LANDING_ICONS.invoice, text: "Three-way match ✓", sub: "INV-2026-0311", className: "-left-52 bottom-20" },
];

/** The dashboard, rising into view under the hero with floating document cards around it. */
export default function ProductPreview() {
  const root = useRef<HTMLElement>(null);

  useGSAP(
    () => {
      const mm = gsap.matchMedia();
      mm.add(MOTION_OK, () => {
        gsap.fromTo(
          "[data-reveal]",
          { y: 30, autoAlpha: 0 },
          { y: 0, autoAlpha: 1, duration: 0.8, stagger: 0.12, ease: "power3.out", scrollTrigger: { trigger: root.current, start: "top 85%", once: true } }
        );
        gsap.fromTo(
          "[data-preview-window]",
          { rotateX: 10, y: 30, opacity: 0.95 },
          { rotateX: 0, y: 0, opacity: 1, ease: "none", transformPerspective: 1200, scrollTrigger: { trigger: "[data-preview-window]", start: "top 95%", end: "top 45%", scrub: 1 } }
        );
        gsap.fromTo(
          "[data-preview-chip]",
          { scale: 0.7, autoAlpha: 0 },
          { scale: 1, autoAlpha: 1, duration: 0.6, stagger: 0.15, ease: "back.out(1.7)", scrollTrigger: { trigger: "[data-preview-window]", start: "top 70%", once: true } }
        );
        gsap.utils.toArray<HTMLElement>("[data-preview-chip]").forEach((chip, i) => {
          gsap.to(chip, { y: i % 2 ? 8 : -8, duration: 2.6 + i * 0.4, repeat: -1, yoyo: true, ease: "sine.inOut" });
        });
      });
    },
    { scope: root }
  );

  return (
    <section ref={root} className="relative overflow-hidden px-5 pb-24 pt-8 sm:px-8">
      <SectionHeading
        eyebrow="One screen for the whole chain"
        title="See where every purchase stands"
        lead="Spend, open orders, payables and delivery quality, computed from live documents and limited to what each person may see."
      />
      <div className="relative mx-auto mt-14 max-w-5xl">
        <div data-preview-window className="w-full">
          <DashboardPreview />
        </div>
        {CHIPS.map((chip) => (
          <div
            key={chip.sub}
            data-preview-chip
            className={`absolute z-10 hidden w-56 items-center gap-3 rounded-xl border border-gray-200 bg-white/95 px-4 py-3 shadow-theme-lg backdrop-blur 2xl:flex dark:border-white/10 dark:bg-gray-900/95 ${chip.className}`}
          >
            <span className="flex size-9 items-center justify-center rounded-lg bg-brand-50 text-brand-500 dark:bg-brand-500/15">{chip.icon}</span>
            <span>
              <span className="block text-sm font-semibold text-gray-800 dark:text-white/90">{chip.text}</span>
              <span className="block font-mono text-[11px] text-gray-400">{chip.sub}</span>
            </span>
          </div>
        ))}
      </div>
    </section>
  );
}
