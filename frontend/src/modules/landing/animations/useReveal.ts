import type { RefObject } from "react";
import { gsap, MOTION_OK, ScrollTrigger, useGSAP } from "./gsapSetup";

/**
 * Fades and lifts every `[data-reveal]` element inside `scope` as it scrolls into view, in small staggered
 * batches. Elements stay visible when the visitor prefers reduced motion.
 */
export default function useReveal(scope: RefObject<HTMLElement | null>) {
  useGSAP(
    () => {
      const mm = gsap.matchMedia();
      mm.add(MOTION_OK, () => {
        const items = gsap.utils.toArray<HTMLElement>("[data-reveal]");
        if (items.length === 0) return;
        gsap.set(items, { autoAlpha: 0, y: 40 });
        ScrollTrigger.batch(items, {
          start: "top 88%",
          once: true,
          onEnter: (batch) =>
            gsap.to(batch, { autoAlpha: 1, y: 0, duration: 0.9, ease: "power3.out", stagger: 0.12, overwrite: true }),
        });
      });
    },
    { scope }
  );
}
