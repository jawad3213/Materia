import { useRef } from "react";
import { gsap, MOTION_OK, useGSAP } from "../animations/gsapSetup";
import { CYCLE } from "../content/landingContent";
import SectionHeading from "./SectionHeading";
import { CHECK, LANDING_ICONS } from "./landingIcons";

/**
 * The seven stages of the cycle. On large screens the section pins and the stages slide past horizontally as the
 * visitor scrolls, with a progress rail; on small screens they stack and reveal one by one.
 */
export default function ProcurementCycle() {
  const root = useRef<HTMLElement>(null);
  const track = useRef<HTMLDivElement>(null);

  useGSAP(
    () => {
      const mm = gsap.matchMedia();

      mm.add(`${MOTION_OK} and (min-width: 1024px)`, () => {
        const panel = track.current;
        if (!panel) return;
        const distance = () => panel.scrollWidth - window.innerWidth + 96;
        const progress = root.current?.querySelector<HTMLElement>("[data-cycle-progress]");
        if (progress) gsap.set(progress, { scaleX: 0 });

        // The track slides left while the section is pinned; the rail fills with the same progress.
        const slide = gsap.to(panel, {
          x: () => -distance(),
          ease: "none",
          scrollTrigger: {
            trigger: root.current,
            start: "top top",
            end: () => `+=${distance()}`,
            pin: true,
            scrub: 0.8,
            invalidateOnRefresh: true,
            onUpdate: (self) => progress && gsap.set(progress, { scaleX: self.progress }),
          },
        });

        // Each card lights up as it reaches the middle of the screen.
        gsap.utils.toArray<HTMLElement>("[data-cycle-card]").forEach((card) => {
          gsap.fromTo(
            card,
            { opacity: 0.45, scale: 0.94 },
            {
              opacity: 1,
              scale: 1,
              ease: "power1.out",
              scrollTrigger: { trigger: card, containerAnimation: slide, start: "left 75%", end: "left 40%", scrub: true },
            }
          );
        });
      });

      mm.add(`${MOTION_OK} and (max-width: 1023px)`, () => {
        gsap.utils.toArray<HTMLElement>("[data-cycle-card]").forEach((card) => {
          gsap.from(card, { y: 50, autoAlpha: 0, duration: 0.8, ease: "power3.out", scrollTrigger: { trigger: card, start: "top 88%", once: true } });
        });
      });
    },
    { scope: root }
  );

  return (
    <section ref={root} id="cycle" className="relative overflow-hidden bg-gray-950 py-24 text-white lg:flex lg:h-screen lg:flex-col lg:justify-center lg:py-0">
      <div aria-hidden="true" className="pointer-events-none absolute inset-0 bg-[radial-gradient(ellipse_at_top,#465fff40,transparent_60%)]" />
      <div className="relative mx-auto w-full max-w-7xl px-5 sm:px-8">
        <div className="[&_h2]:text-white [&_p]:text-gray-400">
          <SectionHeading
            eyebrow="One connected cycle"
            title="Seven steps. One system. No spreadsheets in between."
            lead="Each step hands its data to the next, so nothing is retyped and every figure can be traced back to the document it came from."
            align="left"
          />
        </div>
        <div className="mt-10 hidden h-1 w-full overflow-hidden rounded-full bg-white/10 lg:block">
          <div data-cycle-progress className="h-full origin-left rounded-full bg-gradient-to-r from-brand-500 via-success-500 to-warning-500" />
        </div>
      </div>

      <div ref={track} className="relative mt-12 flex flex-col gap-5 px-5 sm:px-8 lg:w-max lg:flex-row lg:gap-6 lg:pl-[max(2rem,calc((100vw-80rem)/2+2rem))] lg:pr-24">
        {CYCLE.map((step, index) => (
          <article
            key={step.id}
            data-cycle-card
            className="relative flex w-full flex-col rounded-2xl border border-white/10 bg-white/[0.04] p-7 backdrop-blur lg:w-[360px] lg:shrink-0"
          >
            <div className="flex items-center justify-between">
              <span className="flex size-12 items-center justify-center rounded-xl bg-brand-500/20 text-brand-300">{LANDING_ICONS[step.icon]}</span>
              <span className="font-mono text-sm text-white/30">{String(index + 1).padStart(2, "0")}</span>
            </div>
            <h3 className="mt-6 text-2xl font-semibold">{step.title}</h3>
            <p className="mt-3 text-sm leading-relaxed text-gray-400">{step.summary}</p>
            <ul className="mt-6 space-y-2.5 border-t border-white/10 pt-5">
              {step.detail.map((d) => (
                <li key={d} className="flex items-start gap-2.5 text-sm text-gray-300">
                  <span className="mt-0.5 text-success-400">{CHECK}</span>
                  {d}
                </li>
              ))}
            </ul>
          </article>
        ))}
      </div>
    </section>
  );
}
