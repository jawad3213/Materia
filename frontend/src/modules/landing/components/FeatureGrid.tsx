import { useRef } from "react";
import { gsap, MOTION_OK, ScrollTrigger, useGSAP } from "../animations/gsapSetup";
import { FEATURES } from "../content/landingContent";
import SectionHeading from "./SectionHeading";
import { LANDING_ICONS } from "./landingIcons";

/** The main capabilities, revealed in a staggered wave; each card follows the pointer with a soft spotlight. */
export default function FeatureGrid() {
  const root = useRef<HTMLElement>(null);

  useGSAP(
    () => {
      const mm = gsap.matchMedia();
      mm.add(MOTION_OK, () => {
        gsap.from("[data-reveal]", {
          y: 30,
          autoAlpha: 0,
          duration: 0.8,
          stagger: 0.12,
          ease: "power3.out",
          scrollTrigger: { trigger: root.current, start: "top 80%", once: true },
        });
        const cards = gsap.utils.toArray<HTMLElement>("[data-feature]");
        gsap.set(cards, { autoAlpha: 0, y: 50, rotateX: -8, transformPerspective: 800 });
        ScrollTrigger.batch(cards, {
          start: "top 90%",
          once: true,
          onEnter: (batch) => gsap.to(batch, { autoAlpha: 1, y: 0, rotateX: 0, duration: 0.9, stagger: 0.1, ease: "power3.out" }),
        });
      });
    },
    { scope: root }
  );

  const spotlight = (e: React.MouseEvent<HTMLElement>) => {
    const rect = e.currentTarget.getBoundingClientRect();
    e.currentTarget.style.setProperty("--x", `${e.clientX - rect.left}px`);
    e.currentTarget.style.setProperty("--y", `${e.clientY - rect.top}px`);
  };

  return (
    <section ref={root} id="features" className="mx-auto max-w-7xl px-5 py-28 sm:px-8">
      <SectionHeading
        eyebrow="Built for the way procurement really works"
        title="Controls your finance team trusts, speed your buyers enjoy"
        lead="Every rule below is enforced by the server, so it holds no matter who clicks what."
      />
      <div className="mt-16 grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
        {FEATURES.map((f) => (
          <article
            key={f.title}
            data-feature
            onMouseMove={spotlight}
            className="group relative overflow-hidden rounded-2xl border border-gray-200 bg-white p-6 transition-shadow hover:shadow-theme-lg dark:border-white/10 dark:bg-white/[0.03]"
          >
            <div
              aria-hidden="true"
              className="pointer-events-none absolute inset-0 opacity-0 transition-opacity duration-300 group-hover:opacity-100"
              style={{ background: "radial-gradient(240px circle at var(--x) var(--y), rgba(70,95,255,0.12), transparent 70%)" }}
            />
            <span className="relative flex size-12 items-center justify-center rounded-xl bg-brand-50 text-brand-500 transition-transform duration-300 group-hover:-rotate-6 group-hover:scale-110 dark:bg-brand-500/15">
              {LANDING_ICONS[f.icon]}
            </span>
            <h3 className="relative mt-5 text-lg font-semibold text-gray-900 dark:text-white">{f.title}</h3>
            <p className="relative mt-2 text-sm leading-relaxed text-gray-500 dark:text-gray-400">{f.text}</p>
          </article>
        ))}
      </div>
    </section>
  );
}
