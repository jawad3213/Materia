import { useRef } from "react";
import { gsap, MOTION_OK, useGSAP } from "../animations/gsapSetup";
import SectionHeading from "./SectionHeading";
import { CHECK, LANDING_ICONS } from "./landingIcons";

/** An illustrative line: 50 ordered, 48 accepted at receipt, 48 invoiced at the order price. */
const DOCS = [
  { key: "order", icon: LANDING_ICONS.order, label: "Purchase order", code: "PO-2026-0214", rows: [["Steel bolts M8", "50 × 5.00"], ["Total", "250.00 MAD"]] },
  { key: "receipt", icon: LANDING_ICONS.receipt, label: "Goods receipt", code: "GR-2026-0087", rows: [["Received", "50"], ["Accepted", "48"]] },
  { key: "invoice", icon: LANDING_ICONS.invoice, label: "Supplier invoice", code: "INV-2026-0311", rows: [["Invoiced", "48 × 5.00"], ["Total", "240.00 MAD"]] },
];

/** Shows the three documents converging on one verified result as the section scrolls by. */
export default function ThreeWayMatch() {
  const root = useRef<HTMLElement>(null);

  useGSAP(
    () => {
      const mm = gsap.matchMedia();
      mm.add(MOTION_OK, () => {
        const tl = gsap.timeline({ scrollTrigger: { trigger: "[data-match-stage]", start: "top 75%", end: "bottom 60%", scrub: 1 } });
        tl.from("[data-match-doc]", { y: 60, autoAlpha: 0, stagger: 0.15, duration: 1, ease: "power2.out" })
          .fromTo("[data-match-line]", { strokeDashoffset: 1 }, { strokeDashoffset: 0, duration: 1, stagger: 0.1, ease: "none" }, "-=0.2")
          .from("[data-match-result]", { scale: 0.5, autoAlpha: 0, duration: 0.6, ease: "back.out(2.2)" });
        gsap.from("[data-reveal]", { y: 30, autoAlpha: 0, duration: 0.8, stagger: 0.12, ease: "power3.out", scrollTrigger: { trigger: root.current, start: "top 80%", once: true } });
      });
    },
    { scope: root }
  );

  return (
    <section ref={root} id="matching" className="bg-gradient-to-b from-brand-25 to-white py-28 dark:from-brand-500/[0.06] dark:to-gray-950">
      <div className="mx-auto max-w-7xl px-5 sm:px-8">
        <SectionHeading
          eyebrow="Three-way matching"
          title="You pay for what arrived in good condition. Nothing more."
          lead="Every invoice line is checked against the quantity ordered, the quantity accepted at receipt and the order price. Differences are flagged before anyone verifies the invoice."
        />

        <div data-match-stage className="relative mx-auto mt-16 max-w-5xl">
          <svg className="pointer-events-none absolute inset-0 hidden h-full w-full md:block" viewBox="0 0 1000 420" preserveAspectRatio="none" aria-hidden="true">
            {["M165 170 C 165 300, 500 260, 500 330", "M500 170 L 500 330", "M835 170 C 835 300, 500 260, 500 330"].map((d) => (
              <path key={d} data-match-line d={d} pathLength={1} strokeDasharray="1" fill="none" stroke="#465FFF" strokeWidth="2" strokeOpacity="0.5" />
            ))}
          </svg>

          <div className="relative grid grid-cols-1 gap-5 md:grid-cols-3">
            {DOCS.map((doc) => (
              <div key={doc.key} data-match-doc className="rounded-2xl border border-gray-200 bg-white p-5 shadow-theme-sm dark:border-white/10 dark:bg-gray-900">
                <div className="flex items-center gap-3">
                  <span className="flex size-10 items-center justify-center rounded-lg bg-brand-50 text-brand-500 dark:bg-brand-500/15">{doc.icon}</span>
                  <div>
                    <p className="text-sm font-semibold text-gray-900 dark:text-white">{doc.label}</p>
                    <p className="font-mono text-xs text-gray-400">{doc.code}</p>
                  </div>
                </div>
                <dl className="mt-4 space-y-2 border-t border-gray-100 pt-4 text-sm dark:border-white/5">
                  {doc.rows.map(([k, v]) => (
                    <div key={k} className="flex justify-between">
                      <dt className="text-gray-500 dark:text-gray-400">{k}</dt>
                      <dd className="font-medium text-gray-800 dark:text-white/90">{v}</dd>
                    </div>
                  ))}
                </dl>
              </div>
            ))}
          </div>

          <div data-match-result className="relative mx-auto mt-10 flex max-w-md items-center gap-4 rounded-2xl border border-success-200 bg-success-50 p-5 md:mt-28 dark:border-success-500/30 dark:bg-success-500/10">
            <span className="flex size-11 shrink-0 items-center justify-center rounded-full bg-success-500 text-white">{CHECK}</span>
            <div>
              <p className="font-semibold text-success-700 dark:text-success-400">Matched: 48 accepted, 48 invoiced</p>
              <p className="text-sm text-success-700/80 dark:text-success-400/80">The 2 rejected units go back to the supplier as a return.</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
