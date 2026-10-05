import { useRef } from "react";
import { Link } from "react-router-dom";
import Button from "../../../shared/components/ui/button/Button";
import useAuth from "../../auth/hooks/useAuth";
import { gsap, MOTION_OK, useGSAP } from "../animations/gsapSetup";
import { ARROW_RIGHT } from "./landingIcons";

/** Closing call to action: the banner grows into place and its light trail sweeps across. */
export default function CtaBanner({ title, text }: { title: string; text: string }) {
  const { isAuthenticated } = useAuth();
  const root = useRef<HTMLElement>(null);

  useGSAP(
    () => {
      const mm = gsap.matchMedia();
      mm.add(MOTION_OK, () => {
        const tl = gsap.timeline({ scrollTrigger: { trigger: root.current, start: "top 80%", once: true } });
        tl.from("[data-cta-box]", { scale: 0.92, autoAlpha: 0, duration: 0.9, ease: "power3.out" })
          .from("[data-cta-item]", { y: 24, autoAlpha: 0, stagger: 0.1, duration: 0.6, ease: "power2.out" }, "-=0.4")
          .fromTo("[data-cta-sweep]", { xPercent: -120 }, { xPercent: 220, duration: 1.6, ease: "power2.inOut" }, "-=0.6");
      });
    },
    { scope: root }
  );

  return (
    <section ref={root} className="px-5 pb-28 sm:px-8">
      <div data-cta-box className="relative mx-auto max-w-6xl overflow-hidden rounded-3xl bg-brand-950 px-8 py-16 text-center sm:px-16">
        <div aria-hidden="true" className="absolute inset-0 bg-[radial-gradient(circle_at_20%_20%,#465fff66,transparent_45%),radial-gradient(circle_at_80%_80%,#12b76a40,transparent_45%)]" />
        <div aria-hidden="true" data-cta-sweep className="absolute inset-y-0 left-0 w-1/3 -skew-x-12 bg-gradient-to-r from-transparent via-white/10 to-transparent" />
        <h2 data-cta-item className="relative text-3xl font-bold tracking-tight text-white sm:text-title-md">
          {title}
        </h2>
        <p data-cta-item className="relative mx-auto mt-4 max-w-2xl text-lg text-brand-100">
          {text}
        </p>
        <div data-cta-item className="relative mt-10 flex flex-col items-center justify-center gap-3 sm:flex-row">
          <Link to={isAuthenticated ? "/dashboard" : "/login"}>
            <Button endIcon={ARROW_RIGHT} className="bg-white !text-brand-700 hover:bg-brand-50">
              {isAuthenticated ? "Open your dashboard" : "Sign in"}
            </Button>
          </Link>
          <Link to="/features" className="text-sm font-medium text-white/80 underline-offset-4 hover:text-white hover:underline">
            See every feature
          </Link>
        </div>
      </div>
    </section>
  );
}
