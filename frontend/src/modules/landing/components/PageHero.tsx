import { useRef } from "react";
import GridShape from "../../../shared/components/common/GridShape";
import Badge from "../../../shared/components/ui/badge/Badge";
import { gsap, MOTION_OK, SplitText, useGSAP } from "../animations/gsapSetup";

/** Header of the inner public pages: badge, split-text title and lead. */
export default function PageHero({ eyebrow, title, lead }: { eyebrow: string; title: string; lead: string }) {
  const root = useRef<HTMLElement>(null);

  useGSAP(
    () => {
      const mm = gsap.matchMedia();
      mm.add(MOTION_OK, () => {
        const split = SplitText.create("[data-page-title]", { type: "words,lines", mask: "lines" });
        gsap
          .timeline({ defaults: { ease: "power4.out" } })
          .from("[data-page-badge]", { y: 16, autoAlpha: 0, duration: 0.5 })
          .from(split.words, { yPercent: 110, duration: 0.9, stagger: 0.04 }, "-=0.2")
          .from("[data-page-lead]", { y: 20, autoAlpha: 0, duration: 0.7 }, "-=0.5");
        return () => split.revert();
      });
    },
    { scope: root }
  );

  return (
    <section ref={root} className="relative isolate overflow-hidden pb-16 pt-36 sm:pt-44">
      <GridShape />
      <div aria-hidden="true" className="pointer-events-none absolute inset-x-0 top-0 -z-10 h-96 bg-[radial-gradient(ellipse_at_top,#465fff26,transparent_65%)]" />
      <div className="mx-auto max-w-4xl px-5 text-center sm:px-8">
        <div data-page-badge>
          <Badge size="sm" color="primary">
            {eyebrow}
          </Badge>
        </div>
        <h1 data-page-title className="mt-5 text-4xl font-bold leading-[1.1] tracking-tight text-gray-900 sm:text-title-lg dark:text-white">
          {title}
        </h1>
        <p data-page-lead className="mx-auto mt-6 max-w-2xl text-lg leading-relaxed text-gray-500 dark:text-gray-400">
          {lead}
        </p>
      </div>
    </section>
  );
}
