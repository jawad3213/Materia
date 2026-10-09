import { useRef, useState } from "react";
import PageMeta from "../../../shared/components/common/PageMeta";
import Badge from "../../../shared/components/ui/badge/Badge";
import { gsap, MOTION_OK, ScrollTrigger, useGSAP } from "../animations/gsapSetup";
import { MODULES } from "../content/landingContent";
import PageHero from "../components/PageHero";
import CtaBanner from "../components/CtaBanner";
import { CHECK, LANDING_ICONS } from "../components/landingIcons";

/** Every module in detail, with a sticky index that follows the section being read. */
export default function FeaturesPage() {
  const root = useRef<HTMLDivElement>(null);
  const [current, setCurrent] = useState(0);

  useGSAP(
    () => {
      // The index highlights the module in view (with or without motion).
      gsap.utils.toArray<HTMLElement>("[data-module]").forEach((section, index) => {
        ScrollTrigger.create({ trigger: section, start: "top 55%", end: "bottom 55%", onToggle: (self) => self.isActive && setCurrent(index) });
      });

      const mm = gsap.matchMedia();
      mm.add(MOTION_OK, () => {
        gsap.utils.toArray<HTMLElement>("[data-module]").forEach((section) => {
          const tl = gsap.timeline({ scrollTrigger: { trigger: section, start: "top 75%", once: true } });
          tl.from(section.querySelectorAll("[data-module-text] > *"), { y: 30, autoAlpha: 0, duration: 0.7, stagger: 0.08, ease: "power3.out" })
            .from(section.querySelector("[data-module-visual]"), { y: 40, autoAlpha: 0, rotate: -2, duration: 0.9, ease: "power3.out" }, "-=0.6")
            .from(section.querySelectorAll("[data-flow-step]"), { scale: 0.7, autoAlpha: 0, duration: 0.4, stagger: 0.12, ease: "back.out(2)" }, "-=0.4")
            .from(section.querySelectorAll("[data-flow-link]"), { scaleX: 0, transformOrigin: "left center", duration: 0.3, stagger: 0.12 }, "<");
          // The visual drifts slightly slower than the page.
          gsap.to(section.querySelector("[data-module-visual]"), {
            yPercent: -6,
            ease: "none",
            scrollTrigger: { trigger: section, start: "top bottom", end: "bottom top", scrub: true },
          });
        });
      });
    },
    { scope: root }
  );

  return (
    <div ref={root}>
      <PageMeta title="Features | Materia" description="Master data, requisitions, purchase orders, receipts, returns, invoices, payments and the dashboard." />
      <PageHero
        eyebrow="Features"
        title="Everything procurement needs, in the order it happens"
        lead="Eight modules that share their data, so an approved request becomes an order, a receipt, an invoice and a payment without being retyped."
      />

      <div className="mx-auto grid max-w-7xl gap-12 px-5 pb-24 sm:px-8 lg:grid-cols-[220px_1fr]">
        <nav className="sticky top-28 hidden h-max lg:block" aria-label="Modules">
          <p className="mb-4 text-xs font-semibold uppercase tracking-widest text-gray-400">Modules</p>
          <ul className="space-y-1 border-l border-gray-200 dark:border-white/10">
            {MODULES.map((m, index) => (
              <li key={m.eyebrow}>
                <a
                  href={`#module-${index}`}
                  className={`-ml-px block border-l-2 py-1.5 pl-4 text-sm transition-colors ${
                    current === index ? "border-brand-500 font-medium text-brand-600 dark:text-brand-300" : "border-transparent text-gray-500 hover:text-gray-800 dark:text-gray-400"
                  }`}
                >
                  {m.eyebrow}
                </a>
              </li>
            ))}
          </ul>
        </nav>

        <div className="space-y-28">
          {MODULES.map((m, index) => (
            <section key={m.eyebrow} id={`module-${index}`} data-module className="scroll-mt-28">
              <div className={`grid items-center gap-10 xl:grid-cols-2 ${index % 2 ? "xl:[&>*:first-child]:order-2" : ""}`}>
                <div data-module-text>
                  <Badge size="sm" color="primary">
                    {m.eyebrow}
                  </Badge>
                  <h2 className="mt-4 text-2xl font-bold tracking-tight text-gray-900 sm:text-title-sm dark:text-white">{m.title}</h2>
                  <p className="mt-4 leading-relaxed text-gray-500 dark:text-gray-400">{m.text}</p>
                  <ul className="mt-6 space-y-3">
                    {m.points.map((point) => (
                      <li key={point} className="flex items-start gap-3 text-gray-700 dark:text-gray-300">
                        <span className="mt-0.5 flex size-5 items-center justify-center rounded-full bg-success-50 text-success-600 dark:bg-success-500/15 dark:text-success-400">
                          {CHECK}
                        </span>
                        {point}
                      </li>
                    ))}
                  </ul>
                </div>

                <div data-module-visual className="relative overflow-hidden rounded-3xl border border-gray-200 bg-gradient-to-br from-brand-25 to-white p-8 dark:border-white/10 dark:from-brand-500/[0.08] dark:to-transparent">
                  <div aria-hidden="true" className="absolute -right-10 -top-10 size-40 rounded-full bg-brand-500/10 blur-2xl" />
                  <div className="relative flex items-center gap-4">
                    <span className="flex size-14 items-center justify-center rounded-2xl bg-brand-500 text-white shadow-theme-lg">{LANDING_ICONS[m.icon]}</span>
                    <div>
                      <p className="text-sm text-gray-500 dark:text-gray-400">Lifecycle</p>
                      <p className="font-semibold text-gray-900 dark:text-white">{m.eyebrow}</p>
                    </div>
                  </div>
                  <ol className="relative mt-8 flex flex-wrap items-center gap-y-3">
                    {m.flow.map((step, i) => (
                      <li key={step} className="flex items-center">
                        <span
                          data-flow-step
                          className={`rounded-full px-3.5 py-1.5 text-sm font-medium ${
                            i === m.flow.length - 1
                              ? "bg-success-500 text-white"
                              : "border border-gray-200 bg-white text-gray-700 dark:border-white/10 dark:bg-gray-900 dark:text-gray-200"
                          }`}
                        >
                          {step}
                        </span>
                        {i < m.flow.length - 1 && <span data-flow-link className="mx-2 h-px w-6 bg-brand-300 dark:bg-brand-500/50" />}
                      </li>
                    ))}
                  </ol>
                </div>
              </div>
            </section>
          ))}
        </div>
      </div>

      <CtaBanner title="See it with your own data" text="Sign in and the dashboard fills itself from your requisitions, orders, receipts, invoices and payments." />
    </div>
  );
}
