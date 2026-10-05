import { useRef } from "react";
import { gsap, MOTION_OK, useGSAP } from "../animations/gsapSetup";

/** Facts about the product itself (not about customers): they count up as the strip scrolls into view. */
const FACTS = [
  { value: 7, suffix: "", label: "stages of the procurement cycle, connected" },
  { value: 3, suffix: "-way", label: "invoice matching against orders and receipts" },
  { value: 3, suffix: "", label: "roles with permissions enforced on the server" },
  { value: 0, suffix: "", label: "totals that silently add up different currencies" },
];

export default function ProofStrip() {
  const root = useRef<HTMLElement>(null);

  useGSAP(
    () => {
      const mm = gsap.matchMedia();
      mm.add(MOTION_OK, () => {
        gsap.utils.toArray<HTMLElement>("[data-count]").forEach((el) => {
          const target = Number(el.dataset.count);
          const counter = { value: target === 0 ? 12 : 0 };
          gsap.to(counter, {
            value: target,
            duration: 1.6,
            ease: "power2.out",
            snap: { value: 1 },
            onUpdate: () => {
              el.textContent = String(Math.round(counter.value));
            },
            scrollTrigger: { trigger: el, start: "top 90%", once: true },
          });
        });
        gsap.from("[data-fact]", {
          y: 30,
          autoAlpha: 0,
          duration: 0.8,
          stagger: 0.1,
          ease: "power3.out",
          scrollTrigger: { trigger: root.current, start: "top 85%", once: true },
        });
      });
    },
    { scope: root }
  );

  return (
    <section ref={root} className="border-y border-gray-200 bg-gray-50 py-14 dark:border-white/10 dark:bg-white/[0.02]">
      <div className="mx-auto grid max-w-7xl grid-cols-2 gap-8 px-5 sm:px-8 lg:grid-cols-4">
        {FACTS.map((fact) => (
          <div key={fact.label} data-fact className="text-center lg:text-left">
            <p className="text-4xl font-bold tracking-tight text-gray-900 sm:text-5xl dark:text-white">
              <span data-count={fact.value}>{fact.value}</span>
              <span className="text-brand-500">{fact.suffix}</span>
            </p>
            <p className="mt-2 text-sm text-gray-500 dark:text-gray-400">{fact.label}</p>
          </div>
        ))}
      </div>
    </section>
  );
}
