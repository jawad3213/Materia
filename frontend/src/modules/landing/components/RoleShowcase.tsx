import { useRef, useState } from "react";
import { gsap, MOTION_OK, useGSAP } from "../animations/gsapSetup";
import { ROLES, type Role } from "../content/landingContent";
import SectionHeading from "./SectionHeading";
import useReveal from "../animations/useReveal";
import { CHECK } from "./landingIcons";

/** One tab per role; switching tabs slides the new role's abilities in. */
export default function RoleShowcase() {
  const root = useRef<HTMLElement>(null);
  const panel = useRef<HTMLDivElement>(null);
  const [active, setActive] = useState<Role["id"]>(ROLES[0].id);
  const role = ROLES.find((r) => r.id === active) ?? ROLES[0];
  useReveal(root);

  useGSAP(
    () => {
      const mm = gsap.matchMedia();
      mm.add(MOTION_OK, () => {
        gsap.fromTo("[data-role-item]", { x: 30, autoAlpha: 0 }, { x: 0, autoAlpha: 1, duration: 0.5, stagger: 0.07, ease: "power2.out" });
        gsap.fromTo("[data-role-head]", { y: 12, autoAlpha: 0 }, { y: 0, autoAlpha: 1, duration: 0.45, ease: "power2.out" });
      });
    },
    { scope: panel, dependencies: [active] }
  );

  return (
    <section ref={root} id="roles" className="mx-auto max-w-7xl px-5 py-28 sm:px-8">
      <div className="grid items-center gap-12 lg:grid-cols-2">
        <div>
          <SectionHeading
            eyebrow="Made for every role"
            title="Everyone sees their part of the chain, and only their part"
            lead="Three roles cover the whole cycle. Each screen and each action follows the permissions of the person signed in."
            align="left"
          />
          <div data-reveal className="mt-8 inline-flex rounded-xl bg-gray-100 p-1 dark:bg-white/5" role="tablist">
            {ROLES.map((r) => (
              <button
                key={r.id}
                type="button"
                role="tab"
                aria-selected={active === r.id}
                onClick={() => setActive(r.id)}
                className={`rounded-lg px-5 py-2.5 text-sm font-medium transition-all ${
                  active === r.id ? "bg-white text-gray-900 shadow-theme-xs dark:bg-gray-800 dark:text-white" : "text-gray-500 hover:text-gray-800 dark:text-gray-400"
                }`}
              >
                {r.title}
              </button>
            ))}
          </div>
        </div>

        <div data-reveal>
          <div ref={panel} className="relative overflow-hidden rounded-3xl border border-gray-200 bg-white p-8 shadow-theme-lg dark:border-white/10 dark:bg-white/[0.03]">
            <div aria-hidden="true" className="absolute -right-16 -top-16 size-48 rounded-full bg-brand-500/10 blur-2xl" />
            <div data-role-head className="relative">
              <p className="text-sm font-medium uppercase tracking-widest text-brand-500">{role.title}</p>
              <p className="mt-2 text-2xl font-semibold text-gray-900 dark:text-white">{role.tagline}</p>
            </div>
            <ul className="relative mt-8 space-y-4">
              {role.can.map((item) => (
                <li key={item} data-role-item className="flex items-center gap-3 rounded-xl border border-gray-100 bg-gray-50 px-4 py-3.5 text-gray-700 dark:border-white/5 dark:bg-white/[0.03] dark:text-gray-200">
                  <span className="flex size-7 items-center justify-center rounded-full bg-success-500 text-white">{CHECK}</span>
                  {item}
                </li>
              ))}
            </ul>
          </div>
        </div>
      </div>
    </section>
  );
}
